package xyz.rodit.snapmod.features.conversations

import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.Callback
import xyz.rodit.snapmod.mappings.ConversationManager
import xyz.rodit.snapmod.util.toUUIDString

class PreventTypingNotifications(context: FeatureContext?) : StealthFeature(context!!) {

    override fun init() {
        setClass(ConversationManager.getMapping())

        putFilters(
            ConversationManager.sendTypingNotification,
            { null },
            { it.args[0].toUUIDString() },
            NullFilter(context, "hide_typing")
        )
    }

    override fun onPostHook(param: MethodHookParam) {
        param.args.filter(Callback::isInstance).map(Callback::wrap).first()?.onSuccess()
    }
}