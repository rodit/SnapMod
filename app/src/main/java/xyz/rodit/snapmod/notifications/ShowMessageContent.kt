package xyz.rodit.snapmod.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.JsonArray
import xyz.rodit.snapmod.Shared
import xyz.rodit.snapmod.arroyo.ArroyoReader
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.logging.log
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.before
import java.io.File
import java.io.InputStream
import java.net.URL

private const val CHANNEL_ID = "snapmod_notifications"

private val imageSig = listOf(
    "ffd8ff", // jpeg
    "1a45dfa3", // webm
    "89504e47", // png
)

class ShowMessageContent(context: FeatureContext) : Feature(context) {

    private val arroyoReader = ArroyoReader(context.appContext)
    private val gson: Gson = Gson()

    private val notifications
        get() = context.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var notificationSmallIcon = 0
    private var notificationId = 0

    override fun init() {
        notificationSmallIcon =
            context.appContext.resources.getIdentifier("icon_v6", "mipmap", Shared.SNAPCHAT_PACKAGE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifications.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "SnapMod Custom Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    override fun performHooks() {
        NotificationHandler.handle.before(context, "show_notification_content") {
            val handler = NotificationHandler.wrap(it.thisObject)
            val data = NotificationData.wrap(handler.data)
            val messageHandler = MessagingNotificationHandler.wrap(handler.handler)
            val idProvider = ConversationIdProvider.wrap(handler.conversationIdProvider)
            val bundle = data.bundle
            val type = bundle.get("type") ?: "unknown"
            val conversationId = bundle.getString("arroyo_convo_id")
            val messageId = bundle.getString("arroyo_message_id")
            if (conversationId.isNullOrBlank() || messageId.isNullOrBlank()) return@before
            bundle.getString("media_info")?.let { mediaInfo ->
                if (!context.config.getBoolean("show_notification_media_previews")) return@before

                val snap = type == "snap"
                val (key, iv) = (if (snap)
                    arroyoReader.getSnapKeyAndIv(conversationId, messageId)
                else
                    arroyoReader.getKeyAndIv(conversationId, messageId)) ?: return@before

                val media = getDownloadUrls(mediaInfo)
                val crypt = AesCrypto(key, iv)
                crypt.decrypt(URL(media[0]).openStream()).use { stream ->
                    val (bitmap, isImage) = generatePreview(stream)
                    if (bitmap == null) return@before

                    val feedId = messageHandler.conversationRepository.getFeedId(conversationId)
                    val group = idProvider.conversationIdentifier.group

                    val title = (bundle.getString("ab_cnotif_body") ?: "sent Media") +
                    if (snap)
                        " (${if (isImage) "Image" else "Video"})"
                    else
                        " (Media x ${media.size})"

                    val notification = NotificationCompat.Builder(context.appContext, CHANNEL_ID)
                        .setContentTitle(bundle.getString("sender") ?: "Unknown Sender")
                        .setContentText(title)
                        .setSmallIcon(notificationSmallIcon)
                        .setLargeIcon(bitmap)
                        .setContentIntent(createContentIntent(feedId, bundle, group))
                        .setAutoCancel(true)
                        .setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(null)
                        )
                        .build()

                    notifications.notify(notificationId++, notification)
                    it.result = null
                }

                return@before
            }

            val content = arroyoReader.getMessageContent(conversationId, messageId) ?: return@before
            bundle.putString("ab_cnotif_body", content)
        }
    }

    private fun getDownloadUrls(mediaInfoJson: String): List<String> {
        val json = gson.fromJson(mediaInfoJson, JsonArray::class.java)
        return json.map { it.asJsonObject.get("directDownloadUrl").asString }.toList()
    }

    private fun createContentIntent(feedId: Long, bundle: Bundle, group: Boolean): PendingIntent {
        val conversationId = bundle.getString("arroyo_convo_id") ?: ""
        val uri = Uri.parse("snapchat://notification/notification_chat/").buildUpon()
            .appendQueryParameter("feed-id", feedId.toString())
            .appendQueryParameter("conversation-id", conversationId)
            .appendQueryParameter("is-group", group.toString())
            .appendQueryParameter("source_type", "CHAT")
            .build()
        val intent = Intent("android.intent.action.VIEW_CHAT", uri)
            .setClassName(Shared.SNAPCHAT_PACKAGE, Shared.SNAPCHAT_PACKAGE + ".LandingPageActivity")
            .putExtra("messageId", bundle.getString("chat_message_id"))
            .putExtra("type", "CHAT")
            .putExtra("fromServerNotification", true)
            .putExtra("notificationId", bundle.getString("n_id"))

        val flags = PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context.appContext, 0, intent, flags)
    }

    private fun generatePreview(stream: InputStream): Pair<Bitmap?, Boolean> {
        val temp = File.createTempFile("snapmod", "tmp", context.appContext.cacheDir)
        temp.outputStream().use(stream::copyTo)

        val sig = ByteArray(4)
        temp.inputStream().use { it.read(sig) }
        val sigString = sig.joinToString("") { "%02x".format(it) }

        val isImage = imageSig.any { sigString.startsWith(it) }
        val preview =
            if (isImage) BitmapFactory.decodeFile(temp.path) else generateVideoPreview(temp)

        temp.delete()
        return preview to isImage
    }

    private fun generateVideoPreview(file: File): Bitmap? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(
                    file,
                    Size(1024, 1024),
                    null
                )
            } else {
                ThumbnailUtils.createVideoThumbnail(
                    file.path,
                    MediaStore.Images.Thumbnails.MINI_KIND
                )
            }
        } catch (e: Exception) {
            log.error("Error creating video thumbnail.", e)
        }

        return null
    }
}