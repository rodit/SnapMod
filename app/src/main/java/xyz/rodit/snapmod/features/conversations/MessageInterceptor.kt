package xyz.rodit.snapmod.features.conversations

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.UUIDUtil

class MessageInterceptor(context: FeatureContext) : StealthFeature(context) {

    override fun init() {
        setClass(ConversationManager.getMapping())

        putFilters(
            ConversationManager.sendMessageWithContent,
            { LocalMessageContent.wrap(it.args[1]).contentType },
            { UUIDUtil.fromSnap(MessageDestinations.wrap(it.args[0]).conversations[0]) },
            ObjectFilter(
                context,
                "hide_screenshot",
                ContentType.STATUS_CONVERSATION_CAPTURE_SCREENSHOT(),
                ContentType.STATUS_CONVERSATION_CAPTURE_RECORD()
            ),
            ObjectFilter(context, "hide_save_gallery", ContentType.STATUS_SAVE_TO_CAMERA_ROLL())
        )

        putFilters(
            ConversationManager.updateMessage,
            { MessageUpdate.wrap(it.args[2]) },
            { UUIDUtil.fromSnap(it.args[0]) },
            ObjectFilter(context, "hide_read", MessageUpdate.READ()),
            ObjectFilter(context, "hide_save", MessageUpdate.SAVE(), MessageUpdate.UNSAVE()),
            ObjectFilter(
                context,
                "hide_screenshot",
                MessageUpdate.SCREENSHOT(),
                MessageUpdate.SCREEN_RECORD()
            ),
            ObjectFilter(context, "hide_replay", MessageUpdate.REPLAY()),
            ObjectFilter(context, "dont_release", MessageUpdate.RELEASE())
        )
    }
}