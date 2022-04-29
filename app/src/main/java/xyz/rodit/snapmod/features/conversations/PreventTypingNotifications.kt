package xyz.rodit.snapmod.features.conversations

import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.Callback
import xyz.rodit.snapmod.mappings.ConversationManager
import xyz.rodit.snapmod.util.UUIDUtil

class PreventTypingNotifications(context: FeatureContext?) : StealthFeature(context!!) {

    override fun init() {
        setClass(ConversationManager.getMapping())
        putFilters(
            ConversationManager.sendTypingNotification,
            { null },
            { UUIDUtil.fromSnap(it.args[0]) },
            NullFilter(context, "hide_typing")
        )
    }

    override fun onPostHook(param: MethodHookParam) {
        param.args.filter { Callback.isInstance(it) }.map { Callback.wrap(it) }.first()?.onSuccess()
    }
}