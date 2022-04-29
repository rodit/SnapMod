package xyz.rodit.snapmod.features.tweaks

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.FriendActionClient
import xyz.rodit.snapmod.mappings.FriendActionRequest

class FriendAddOverride(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Override friend add method (not sure if this works).
        FriendActionClient.sendFriendAction.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("enable_friend_override")) {
                    val request = FriendActionRequest.wrap(param.args[0])
                    if (request.action == "add") {
                        val addMethod =
                            context.config.getString("friend_override_value", "ADDED_BY_USERNAME")
                        request.addedBy = addMethod
                    }
                }
            }
        })
    }
}