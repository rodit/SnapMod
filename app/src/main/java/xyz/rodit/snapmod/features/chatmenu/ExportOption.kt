package xyz.rodit.snapmod.features.chatmenu

import android.content.Intent
import androidx.core.content.FileProvider
import xyz.rodit.snapmod.CustomResources.string.menu_option_export
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.SelectFriendsByUserIds
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExportOption(context: FeatureContext):
    ButtonOption(context, "export_chat", menu_option_export) {

    override fun shouldCreate() = true

    override fun handleEvent(data: String?) {
        if (data == null) return

        val (messages, senders) = context.arroyo.getAllMessages(data)
        val friendData =
            context.instances.friendsRepository.selectFriendsByUserIds(senders.toList())
        val senderMap = friendData.map(SelectFriendsByUserIds::wrap).associateBy { u -> u.userId }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault())

        val temp = File.createTempFile(
            "Snapchat Export ",
            ".txt",
            File(context.appContext.filesDir, "file_manager/media")
        )
        temp.deleteOnExit()
        temp.bufferedWriter().use {
            messages.forEach { m ->
                val username = senderMap[m.senderId]?.displayName ?: "Unknown"
                val dateTime = dateFormat.format(m.timestamp)
                it.append(dateTime)
                    .append(" - ")
                    .append(username)
                    .append(": ")
                    .appendLine(m.content)
            }
        }

        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    context.appContext,
                    "com.snapchat.android.media.fileprovider",
                    temp
                )
            )
        context.activity?.startActivity(Intent.createChooser(intent, "Export Chat"))
    }
}