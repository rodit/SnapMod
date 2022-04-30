package xyz.rodit.snapmod.features.conversations

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ArroyoMessageListDataProvider
import xyz.rodit.snapmod.mappings.ChatContext
import xyz.rodit.snapmod.mappings.PresenceSession
import xyz.rodit.snapmod.util.before

class PreventBitmojiPresence(context: FeatureContext) : StealthFeature(context) {

    private var currentConversationId: String = ""

    override fun init() {
        setClass(PresenceSession.getMapping())

        putFilters(
            PresenceSession.activate,
            { null },
            { currentConversationId },
            NullFilter(context, "hide_bitmoji_presence")
        )

        putFilters(
            PresenceSession.deactivate,
            { null },
            { currentConversationId },
            NullFilter(context, "hide_bitmoji_presence")
        )

        putFilters(
            PresenceSession.processTypingActivity,
            { null },
            { currentConversationId },
            NullFilter(context, "hide_bitmoji_presence")
        )
    }

    override fun performHooks() {
        super.performHooks()

        ArroyoMessageListDataProvider.enterConversation.before {
            currentConversationId = ChatContext.wrap(it.args[0]).conversationId.orEmpty()
        }
    }
}