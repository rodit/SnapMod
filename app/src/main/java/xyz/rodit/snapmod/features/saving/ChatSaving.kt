package xyz.rodit.snapmod.features.saving

import android.util.Log
import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.ResolutionListener
import xyz.rodit.snapmod.UriResolverSubscriber
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.PathManager
import xyz.rodit.snapmod.util.TAG
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.before
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
        ChatMediaHandler.constructors.before {
            chatMediaHandler = ChatMediaHandler.wrap(it.thisObject)
        }

        // Allow save action in chat long-press menu.
        ChatActionHelper.canSave.before(context, "allow_save_snaps") {
            it.result = true
        }

        // Allow save action in chat model.
        ChatModelBase.canSave.before(context, "allow_save_snaps") {
            it.result = true
        }

        // Override save type to gallery to allow saving any snaps.
        ChatModelBase.getSaveType.before(context, "allow_save_snaps") {
            if (ChatModelLiveSnap.isInstance(it.thisObject)
                || ChatModelAudioNote.isInstance(it.thisObject)
            ) {
                it.result = SaveType.SNAPCHAT_ALBUM().instance
            }
        }

        // Map live snap model hashCode to media object for download later.
        ChatModelLiveSnap.constructors.after(context, "allow_save_snaps") {
            val hashCode = it.thisObject.hashCode()
            chatMediaMap[hashCode] = it.args[6]
        }

        // Export non-savable media (live snaps and voice notes).
        SaveToCameraRollActionHandler.exportMedia.before(context, "allow_save_snaps") {
            if (ChatModelLiveSnap.isInstance(it.args[1])) {
                // Convert live snap to saved snap.
                val hashCode = it.args[1].hashCode()
                val media = LiveSnapMedia.wrap(chatMediaMap[hashCode])
                val base = ChatModelBase.wrap(it.args[1])
                it.args[1] = ChatModelSavedSnap(
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
            } else if (ChatModelAudioNote.isInstance(it.args[1])) {
                // Resolve audio uri and resolve through proxy of RxObserver.
                // Note: the content resolver provided by appContext cannot open a stream from the uri.
                val base = ChatModelBase.wrap(it.args[1])
                val audio = ChatModelAudioNote.wrap(it.args[1])
                val dest = PathManager.getUri(
                    context.config,
                    PathManager.DOWNLOAD_AUDIO_NOTE,
                    mapOf("id" to base.senderId),
                    ".aac"
                )

                XposedBridge.log("Downloading audio note from " + audio.uri + " to " + dest + ".")
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

                it.result = null
            }
        }
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
