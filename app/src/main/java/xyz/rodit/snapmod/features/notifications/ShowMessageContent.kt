package xyz.rodit.snapmod.features.notifications

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.app.NotificationCompat
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.logging.*
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.toMax
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

private val IMAGE_SIG = listOf(
    "ffd8ff", // jpeg
    "1a45dfa3", // webm
    "89504e47", // png
)

private const val VIDEO_SNAP_DEBOUNCE = 2500L

class ShowMessageContent(context: FeatureContext) : Feature(context, 84608.toMax()) {

    private var lastVideoSnapMessageTimes: MutableMap<String, Long> = ConcurrentHashMap<String, Long>()

    override fun performHooks() {
        SnapNotificationBuilder.build.after {
            val notification = it.result as Notification
            val extras = notification.extras
            val snapBundle = extras.getBundle("system_notification_extras") ?: return@after
            val type = snapBundle.getString("notification_type") ?: return@after
            val conversationId = snapBundle.getString("conversation_id") ?: return@after
            val messageId = snapBundle.getString("message_id") ?: return@after
            if (type == "CHAT") {
                val content =
                    context.arroyo.getMessageContent(conversationId, messageId) ?: return@after
                extras.putString("android.text", content)
                extras.putString("android.bigText", content)
            } else if (type == "SNAP") {
                val time = System.currentTimeMillis()
                if (time - (lastVideoSnapMessageTimes[conversationId] ?: 0L) < VIDEO_SNAP_DEBOUNCE) {
                    it.result = null
                    return@after
                }
                val (key, iv, urlKey) = context.arroyo.getSnapData(conversationId, messageId)
                    ?: return@after
                val mediaUrl = "https://cf-st.sc-cdn.net/h/$urlKey"
                val crypt = AesCrypto(key, iv)
                crypt.decrypt(URL(mediaUrl).openStream()).use { stream ->
                    val (bitmap, isImage) = generatePreview(stream)
                    if (bitmap == null) return@use
                    if (!isImage) lastVideoSnapMessageTimes[conversationId] = time

                    val title =
                        extras.getString("android.text") + " (${if (isImage) "Image" else "Video"})"

                    it.result = NotificationCompat.Builder(context.appContext, notification)
                        .setContentText(title)
                        .setLargeIcon(bitmap)
                        .setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(null)
                        )
                        .build()
                }
            }
        }
    }

    private fun generatePreview(stream: InputStream): Pair<Bitmap?, Boolean> {
        val temp = File.createTempFile("snapmod", "tmp", context.appContext.cacheDir)
        temp.outputStream().use(stream::copyTo)

        val sig = ByteArray(4)
        temp.inputStream().use { it.read(sig) }
        val sigString = sig.joinToString("") { "%02x".format(it) }

        val isImage = IMAGE_SIG.any { sigString.startsWith(it) }
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