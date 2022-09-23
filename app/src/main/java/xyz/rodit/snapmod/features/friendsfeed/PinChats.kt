package xyz.rodit.snapmod.features.friendsfeed

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.FriendsFeedConfig
import xyz.rodit.snapmod.mappings.FriendsFeedView
import xyz.rodit.snapmod.util.after

class PinChats(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        FriendsFeedConfig.constructors.after(context, "allow_pin_chats") {
            FriendsFeedConfig.wrap(it.thisObject).isPinConversationsEnabled = true
        }

        FriendsFeedView.constructors.after(context, "allow_pin_chats") {
            val view = FriendsFeedView.wrap(it.thisObject)
            if (context.pinned.isEnabled(view.key)) {
                view.pinnedTimestamp = view.lastInteractionTimestamp
            }
        }
    }
}
