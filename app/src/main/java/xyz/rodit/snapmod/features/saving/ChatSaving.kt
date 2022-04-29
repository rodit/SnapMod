package xyz.rodit.snapmod.features.saving

import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.ResolutionListener
import xyz.rodit.snapmod.UriResolverSubscriber
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.PathManager
import xyz.rodit.snapmod.util.TAG
import xyz.rodit.xposed.client.http.StreamProvider
import xyz.rodit.xposed.client.http.streams.CachedStreamProvider
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider
import java.io.IOException
import java.lang.reflect.Proxy
import java.util.UUID

class ChatSaving(context: FeatureContext) : Feature(context) {

    private val chatMediaMap: MutableMap<Int, Any> = HashMap()

    private var chatMediaHandler: ChatMediaHandler? = null

    override fun performHooks() {
        // Obtain chat media handler instance
        ChatMediaHandler.constructors.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                chatMediaHandler = ChatMediaHandler.wrap(param.thisObject)
            }
        })

        // Allow save action in chat long-press menu.
        ChatActionHelper.canSave.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("allow_save_snaps")) {
                    param.result = true
                }
            }
        })

        // Allow save action in chat model.
        ChatModelBase.canSave.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("allow_save_snaps")) {
                    param.result = true
                }
            }
        })

        // Override save type to gallery to allow saving any snaps.
        ChatModelBase.getSaveType.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("allow_save_snaps")
                    && (ChatModelLiveSnap.isInstance(param.thisObject)
                            || ChatModelAudioNote.isInstance(param.thisObject))
                ) {
                    param.result = SaveType.SNAPCHAT_ALBUM().instance
                }
            }
        })

        // Map live snap model hashCode to media object for download later.
        ChatModelLiveSnap.constructors.hook(object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("allow_save_snaps")) {
                    val hashCode = param.thisObject.hashCode()
                    chatMediaMap[hashCode] = param.args[6]
                }
            }
        })

        // Export non-savable media (live snaps and voice notes).
        SaveToCameraRollActionHandler.exportMedia.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!context.config.getBoolean("allow_save_snaps")) return

                if (ChatModelLiveSnap.isInstance(param.args[1])) {
                    // Convert live snap to saved snap.
                    val hashCode = param.args[1].hashCode()
                    val media = LiveSnapMedia.wrap(chatMediaMap[hashCode])
                    val base = ChatModelBase.wrap(param.args[1])
                    param.args[1] = ChatModelSavedSnap(
                        base.context,
                        base.messageData,
                        base.senderId,
                        emptyMap<Any?, Any>(),
                        true,
                        base.reactionsViewModel,
                        true,
                        0,
                        0,
                        media,
                        null,
                        base.status,
                        true,
                        true
                    ).instance
                } else if (ChatModelAudioNote.isInstance(param.args[1])) {
                    // Resolve audio uri and resolve through proxy of RxObserver.
                    // Note: the content resolver provided by appContext cannot open a stream from the uri.
                    val base = ChatModelBase.wrap(param.args[1])
                    val audio = ChatModelAudioNote.wrap(param.args[1])
                    val dest = PathManager.getUri(
                        context.config,
                        PathManager.DOWNLOAD_AUDIO_NOTE,
                        mapOf("id" to base.senderId),
                        ".aac"
                    )

                    XposedBridge.log(
                        "Downloading audio note from " + audio.uri + " to " + dest + "."
                    )
                    val observerProxy = Proxy.newProxyInstance(
                        context.classLoader,
                        arrayOf(RxObserver.getMappedClass()),
                        MediaUriDownloader(context, dest)
                    )

                    chatMediaHandler!!.resolve(
                        audio.uri,
                        emptySet<Any>(),
                        true,
                        emptySet<Any>()
                    ).subscribe(RxObserver.wrap(observerProxy))

                    param.result = null
                }
            }
        })
    }

    private class MediaUriDownloader(context: FeatureContext, dest: String) :
        UriResolverSubscriber(UriListener(context, dest)) {

        private class UriListener(private val context: FeatureContext, private val dest: String) :
            ResolutionListener {

            override fun invoke(result: Any?) {
                Log.d(TAG, "Accepted media stream provider: $result")
                val streamProvider = MediaStreamProvider.wrap(result)
                val uuid = UUID.randomUUID().toString()
                val provider: StreamProvider =
                    CachedStreamProvider(FileProxyStreamProvider(context.appContext) { streamProvider.mediaStream })
                try {
                    provider.provide()
                } catch (e: IOException) {
                    Log.e(TAG, "Error pre-providing cached stream.", e)
                }
                context.server.mapStream(uuid, provider)
                context.files.download(
                    context.config.getBoolean("use_android_download_manager", true),
                    context.server.root + "/" + uuid,
                    dest,
                    "Audio Note",
                    null
                )
            }
        }
    }
}
