package xyz.rodit.snapmod.features.tweaks
/*
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ComposerFriend
import xyz.rodit.snapmod.mappings.DisplayInfoContainer
import xyz.rodit.snapmod.mappings.FriendListener
import xyz.rodit.snapmod.mappings.ProfileMyFriendsSection
import xyz.rodit.snapmod.util.before

class HideFriends(context: FeatureContext) : Feature(context) {

    private val hiddenFriends: MutableSet<String> = HashSet()

    override fun onConfigLoaded(first: Boolean) {
        hiddenFriends.clear()
        for (username in context.config.getString("hidden_friends", "").split("\n")
            .filter { it.isNotBlank() }) {
            hiddenFriends.add(username.trim())
        }
    }

    override fun performHooks() {
        // Hide friends from 'My Friends' in profile.
        ProfileMyFriendsSection.filter.before(context, "hide_friends") {
            val list = it.args[0] as List<*>
            it.args[0] = list.filter { friend ->
                !hiddenFriends.contains(
                    DisplayInfoContainer.wrap(friend).term
                )
            }
        }

        // Hide friends from best friends list.
        FriendListener.handle.before(context, "hide_friends") {
            if (it.args[0] is List<*>) {
                val list = it.args[0] as List<*>
                if (list.isEmpty() || !ComposerFriend.isInstance(list[0])) {
                    return@before
                }

                it.args[0] =
                    list.filter { friend ->
                        !hiddenFriends.contains(
                            ComposerFriend.wrap(friend).user.username
                        )
                    }
            }
        }
    }
}
*/