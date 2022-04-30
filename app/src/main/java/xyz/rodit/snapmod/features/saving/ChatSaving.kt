package xyz.rodit.snapmod.features.saving

import android.util.Log
import xyz.rodit.snapmod.ResolutionListener
import xyz.rodit.snapmod.UriResolverSubscriber
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.*
import xyz.rodit.xposed.client.http.StreamProvider
import xyz.rodit.xposed.client.http.streams.CachedStreamProvider
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Proxy

class ChatSaving(context: FeatureContext) : Feature(context) {

    private val chatMediaMap: MutableMap<Int, Any> = HashMap()

    private var chatMediaHandler: ChatMediaHandler? = null
    private var lastMessageData: MessageDataModel? = null

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
            if (!ChatModelBase.isInstance(it.args[1])) return@before
            val base = ChatModelBase.wrap(it.args[1])
            lastMessageData = base.messageData

            if (ChatModelLiveSnap.isInstance(it.args[1])) {
                // Convert live snap to saved snap.
                val hashCode = it.args[1].hashCode()
                val media = LiveSnapMedia.wrap(chatMediaMap[hashCode])
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
                val audio = ChatModelAudioNote.wrap(it.args[1])

                val observerProxy = Proxy.newProxyInstance(
                    context.classLoader,
                    arrayOf(RxObserver.getMappedClass()),
                    MediaUriDownloader(
                        context,
                        PathManager.DOWNLOAD_AUDIO_NOTE,
                        mapOf(
                            "id" to base.senderId,
                            "u" to base.messageData.senderUsername,
                            "d" to base.messageData.senderDisplayName
                        ),
                        ".aac"
                    )
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

        MediaExportControllerImpl.exportMedia.before(context, "enable_custom_snap_download_path") {
            val messageData = lastMessageData
            if (messageData?.isNotNull != true) return@before
            val export = ExportItem.wrap(it.args[2])

            val provider = FileProxyStreamProvider(context.appContext) {
                FileInputStream(
                    export.uri.path?.let(::File)
                )
            }

            context.download(
                PathManager.DOWNLOAD_SNAP,
                mapOf(
                    "u" to messageData.senderUsername,
                    "id" to messageData.senderId,
                    "d" to messageData.senderDisplayName
                ),
                '.' + export.fileName.orEmpty().split('.').last(),
                provider,
                "${lastMessageData!!.senderDisplayName}'s snap"
            )

            it.result = null
        }
    }

    private class MediaUriDownloader(context: FeatureContext, type: String, paramsMap: Map<String, String>, extension: String) :
        UriResolverSubscriber(UriListener(context, type, paramsMap, extension)) {

        private class UriListener(
            private val context: FeatureContext, private val type: String,
            private val paramsMap: Map<String, String>, private val extension: String
        ) :
            ResolutionListener {

            override fun invoke(result: Any?) {
                Log.d(TAG, "Accepted media stream provider: $result")
                val streamProvider = MediaStreamProvider.wrap(result)
                val provider: StreamProvider =
                    CachedStreamProvider(FileProxyStreamProvider(context.appContext) { streamProvider.mediaStream })
                try {
                    provider.provide()
                } catch (e: IOException) {
                    Log.e(TAG, "Error pre-providing cached stream.", e)
                }

                context.download(type, paramsMap, extension, provider, "Media")
            }
        }
    }
}
