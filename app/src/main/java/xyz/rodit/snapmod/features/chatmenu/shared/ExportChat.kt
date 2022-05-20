package xyz.rodit.snapmod.features.chatmenu.shared

import android.content.Intent
import androidx.core.content.FileProvider
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.SelectFriendsByUserIds
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun export(context: FeatureContext, key: String) {
    val (messages, senders) = context.arroyo.getAllMessages(key)
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