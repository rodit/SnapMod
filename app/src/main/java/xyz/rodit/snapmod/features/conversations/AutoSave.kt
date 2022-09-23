package xyz.rodit.snapmod.features.conversations

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.logging.log
import xyz.rodit.snapmod.mappings.ArroyoConvertMessagesAction
import xyz.rodit.snapmod.mappings.ChatCommandSource
import xyz.rodit.snapmod.mappings.Message
import xyz.rodit.snapmod.util.*

class AutoSave(context: FeatureContext) : Feature(context, 84608.toMax()) {

    private val messageTypes = hashSetOf<String>()

    override fun onConfigLoaded(first: Boolean) {
        messageTypes.clear()
        messageTypes.addAll(context.config.getList("auto_save_types"))
    }

    override fun performHooks() {
        ArroyoConvertMessagesAction.apply.before {
            if (it.args[0] !is List<*>) return@before
            if (context.instances.chatCommandsClient.isNull) {
                log.debug("Cannot auto-save messages. Chat commands client was null.")
                return@before
            }

            val messages =
                (it.args[0] as List<*>).map { p -> p?.pairFirst }
                    .filter(Message::isInstance)
                    .map { m -> Message.wrap(m) }
            messages.filter(Message::isNotNull).forEach { m ->
                val descriptor = m.descriptor
                val conversationId = descriptor.conversationId.toUUIDString()

                if (!context.config.getBoolean("auto_save_all_chats")
                    && !context.autoSave.isEnabled(conversationId)
                ) return@forEach

                val contentType = m.messageContent.contentType.instance.toString()
                if (!messageTypes.contains(contentType)) return@forEach

                val savedBy = m.metadata.savedBy
                if (!savedBy.isNullOrEmpty()) return@forEach

                val arroyoId = createArroyoId(conversationId, descriptor.messageId)
                context.instances.chatCommandsClient.saveMessage(
                    null,
                    arroyoId,
                    true,
                    false,
                    ChatCommandSource.CHAT(),
                    false
                )
            }
        }
    }

    private fun createArroyoId(conversationId: String, messageId: Long): String {
        return "$conversationId:arroyo-m-id:$messageId"
    }
}