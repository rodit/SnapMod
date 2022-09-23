package xyz.rodit.snapmod.features.saving

import android.net.Uri
import xyz.rodit.snapmod.ResolutionListener
import xyz.rodit.snapmod.Shared
import xyz.rodit.snapmod.UriResolverSubscriber
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.logging.log
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.PathManager
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.before
import xyz.rodit.snapmod.util.download
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
                || ChatModelPlugin.isInstance(it.thisObject)
            ) {
                it.result = SaveType.SNAPCHAT_ALBUM().instance
            }
        }

        // Map live snap model hashCode to media object for download later.
        ChatModelWithMedia.constructors.after(context, "allow_save_snaps") {
            val hashCode = it.thisObject.hashCode()
            chatMediaMap[hashCode] = it.args[6]
        }

        // Export non-savable media (live snaps and voice notes).
        ActionMenuPresenter.handleAction.before {
            if (it.args[3] != ChatMenuItemType.SAVE_TO_CAMERA_ROLL().instance) return@before

            val base = ChatModelBase.wrap(it.args[2])
            lastMessageData = base.messageData

            when {
                ChatModelWithMedia.isInstance(it.args[2]) -> {
                    // Convert live snap to saved snap.
                    val hashCode = it.args[2].hashCode()
                    val media = LiveSnapMedia.wrap(chatMediaMap[hashCode])
                    it.args[2] = ChatModelSavedSnap(
                        base.context,
                        base.messageData,
                        base.senderId,
                        emptyMap<Any?, Any>(),
                        true,
                        null,
                        true,
                        0,
                        0,
                        media,
                        null,
                        base.status,
                        true
                    ).instance
                }
                ChatModelPlugin.isInstance(it.args[2]) -> {
                    val messageData = base.messageData

                    if (messageData.type != "audio_note") return@before
                    val media = GallerySnapMedia.wrap(messageData.media.instance).media
                    val uri = createMediaUri(messageData.arroyoMessageId, media.id)

                    resolveAndDownload(uri, messageData)
                    it.result = null
                }
            }
        }

        // Override snap save location by custom-downloading media instead.
        MediaExportControllerImpl.exportMedia.before(context, "enable_custom_snap_download_path") {
            val messageData = lastMessageData
            if (messageData?.isNotNull != true) return@before
            val destination = DestinationInfo.wrap(it.args[2])

            val provider = FileProxyStreamProvider(context.appContext) {
                FileInputStream(
                    destination.actualFileName?.let(::File)
                )
            }

            context.download(
                PathManager.DOWNLOAD_SNAP,
                createParams(messageData),
                '.' + destination.actualFileName.orEmpty().split('.').last(),
                provider,
                "${lastMessageData!!.senderDisplayName}'s snap"
            )

            it.result = null
        }
    }

    private fun createParams(messageData: MessageDataModel): Map<String, String> {
        return mapOf(
            "d" to messageData.senderDisplayName,
            "id" to messageData.senderId,
            "u" to messageData.senderUsername
        )
    }

    private fun resolveAndDownload(uri: Uri, messageData: MessageDataModel) {
        // Resolve audio uri and resolve through proxy of RxObserver.
        // Note: the content resolver provided by appContext cannot open a stream from the uri.
        val observerProxy = Proxy.newProxyInstance(
            context.classLoader,
            arrayOf(RxObserver.getMappedClass()),
            MediaUriDownloader(
                context,
                PathManager.DOWNLOAD_AUDIO_NOTE,
                createParams(messageData),
                ".aac"
            )
        )

        chatMediaHandler!!.resolve(
            uri,
            emptySet<Any>(),
            true,
            emptySet<Any>()
        ).subscribe(RxObserver.wrap(observerProxy))
    }

    private fun createMediaUri(messageId: String, mediaId: String): Uri {
        return Uri.Builder().scheme("content").authority("${Shared.SNAPCHAT_PACKAGE}.provider")
            .appendPath("chat_media")
            .appendPath(messageId)
            .appendPath(mediaId)
            .appendQueryParameter("target", "DEFAULT")
            .build()
    }

    private class MediaUriDownloader(
        context: FeatureContext, type: String, paramsMap: Map<String, String>, extension: String
    ) :
        UriResolverSubscriber(UriListener(context, type, paramsMap, extension)) {

        private class UriListener(
            private val context: FeatureContext, private val type: String,
            private val paramsMap: Map<String, String>, private val extension: String
        ) :
            ResolutionListener {

            override fun invoke(result: Any?) {
                log.debug("Accepted media stream provider: $result")
                val streamProvider = MediaStreamProvider.wrap(result)
                val provider: StreamProvider =
                    CachedStreamProvider(FileProxyStreamProvider(context.appContext) { streamProvider.mediaStream })
                try {
                    provider.provide()
                } catch (e: IOException) {
                    log.error("Error pre-providing cached stream.", e)
                }

                context.download(type, paramsMap, extension, provider, "Media")
            }
        }
    }
}