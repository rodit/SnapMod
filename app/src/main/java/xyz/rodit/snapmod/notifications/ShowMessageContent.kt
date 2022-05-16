package xyz.rodit.snapmod.notifications

import xyz.rodit.snapmod.arroyo.ArroyoReader
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.NotificationData
import xyz.rodit.snapmod.mappings.NotificationHandler
import xyz.rodit.snapmod.util.before

class ShowMessageContent(context: FeatureContext) : Feature(context) {

    private val arroyoReader = ArroyoReader(context.appContext)

    override fun performHooks() {
        NotificationHandler.handle.before(context, "show_notification_content") {
            val data = NotificationData.wrap(NotificationHandler.wrap(it.thisObject).data)
            val bundle = data.bundle
            val conversationId = bundle.getString("arroyo_convo_id")
            val messageId = bundle.getString("arroyo_message_id")
            if (conversationId.isNullOrBlank() || messageId.isNullOrBlank()) return@before

            val content = arroyoReader.getMessageContent(conversationId, messageId) ?: return@before
            bundle.putString("ab_cnotif_body", content)
        }
    }
}