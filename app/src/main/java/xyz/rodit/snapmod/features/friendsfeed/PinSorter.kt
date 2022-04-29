package xyz.rodit.snapmod.features.friendsfeed

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.FriendsFeedView

class PinSorter(context: FeatureContext?) : FeedSorter(context!!) {

    override fun shouldApply(): Boolean {
        return context.config.getBoolean("allow_pin_chats")
    }

    override fun sort(items: MutableList<FriendsFeedView>): MutableList<FriendsFeedView> {
        val split = items.partition {
            if (context.pinned.isEnabled(it.key)) {
                val friendmojis = it.friendmojiCategories
                if (!friendmojis.contains("pinned")) {
                    it.friendmojiCategories =
                        if (friendmojis.isNullOrBlank()) "pinned" else "$friendmojis,pinned"
                }
                true
            } else {
                false
            }
        }

        return (split.first + split.second).toMutableList()
    }
}
