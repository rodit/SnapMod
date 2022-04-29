package xyz.rodit.snapmod.features.tweaks

import android.text.TextUtils
import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ComposerFriend
import xyz.rodit.snapmod.mappings.DisplayInfoContainer
import xyz.rodit.snapmod.mappings.FriendListener
import xyz.rodit.snapmod.mappings.ProfileMyFriendsSection

class HideFriends(context: FeatureContext) : Feature(context) {

    private val hiddenFriends: MutableSet<String> = HashSet()

    override fun onConfigLoaded(first: Boolean) {
        hiddenFriends.clear()
        for (username in context.config.getString("hidden_friends", "").split("\n")
            .toTypedArray()) {
            if (!TextUtils.isEmpty(username)) {
                hiddenFriends.add(username.trim { it <= ' ' })
            }
        }
    }

    override fun performHooks() {
        // Hide friends from 'My Friends' in profile.
        ProfileMyFriendsSection.filter.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("hide_friends")) {
                    val list = param.args[0] as List<*>
                    param.args[0] =
                        list.filter { !hiddenFriends.contains(DisplayInfoContainer.wrap(it).term) }
                }
            }
        })

        // Hide friends from best friends list.
        FriendListener.handle.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("hide_friends") && param.args[0] is List<*>) {
                    val list = param.args[0] as List<*>
                    if (list.isEmpty() || !ComposerFriend.isInstance(list[0])) {
                        return
                    }

                    param.args[0] =
                        list.filter { !hiddenFriends.contains(ComposerFriend.wrap(it).user.username) }
                }
            }
        })
    }
}