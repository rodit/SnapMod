package xyz.rodit.snapmod.features.conversations

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ArroyoMessageListDataProvider
import xyz.rodit.snapmod.mappings.ChatContext
import xyz.rodit.snapmod.mappings.PresenceSession

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
        ArroyoMessageListDataProvider.enterConversation.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                currentConversationId = ChatContext.wrap(param.args[0]).conversationId.orEmpty()
            }
        })
    }
}