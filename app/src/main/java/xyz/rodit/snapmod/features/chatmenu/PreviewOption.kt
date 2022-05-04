package xyz.rodit.snapmod.features.chatmenu

import android.app.AlertDialog
import xyz.rodit.snapmod.CustomResources
import xyz.rodit.snapmod.createDummyProxy
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.isDummyProxy
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.UUIDUtil
import xyz.rodit.snapmod.util.before
import java.lang.Integer.min

class PreviewOption(context: FeatureContext) :
    ButtonOption(context, "preview", CustomResources.string.menu_option_preview) {

    override fun shouldCreate(): Boolean {
        return true
    }

    override fun handleEvent(data: String?) {
        if (data == null) return

        val uuid = UUIDUtil.toSnap(data)
        val proxy =
            ConversationDummyInterface.wrap(
                ConversationDummyInterface.getMappedClass().createDummyProxy(context.classLoader)
            )

        context.instances.conversationManager.fetchConversationWithMessages(
            uuid,
            DefaultFetchConversationCallback(proxy, uuid, false)
        )
    }

    override fun performHooks() {
        DefaultFetchConversationCallback.onFetchConversationWithMessagesComplete.before {
            if (!DefaultFetchConversationCallback.wrap(it.thisObject).dummy.isDummyProxy) return@before

            val conversation = Conversation.wrap(it.args[0])
            val messageList = it.args[1] as List<*>
            val senderToNumberMap = mutableMapOf<String, Int>()
            val numMessages =
                min(context.config.getInt("preview_messages_count", 5), messageList.size)
            val previewText = StringBuilder("Last ").append(numMessages).append(" messages:")
            messageList.takeLast(numMessages)
                .map(Message::wrap).forEach { m ->
                    run {
                        val uuidString = UUIDUtil.fromSnap(m.senderId)
                        senderToNumberMap.putIfAbsent(uuidString, senderToNumberMap.size)
                        previewText.append('\n').append(senderToNumberMap[uuidString]).append(": ")
                        if (m.messageContent.contentType.instance == ContentType.CHAT().instance) {
                            val chatMessage =
                                NanoMessageContent.parse(m.messageContent.content).chatMessageContent.content
                            previewText.append(chatMessage)
                        } else {
                            previewText.append(m.messageContent.contentType.instance)
                        }
                    }
                }

            context.activity?.runOnUiThread {
                AlertDialog.Builder(context.activity)
                    .setTitle(if (conversation.title.isNullOrBlank()) "Chat Preview" else conversation.title)
                    .setMessage(previewText)
                    .setPositiveButton("Ok") { _, _ -> }
                    .show()
            }
        }
    }
}