package xyz.rodit.snapmod.features.friendsfeed

import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.FriendsFeedView

abstract class FeedSorter(context: FeatureContext) : Contextual(context) {

    abstract fun shouldApply(): Boolean

    abstract fun sort(items: MutableList<FriendsFeedView>): MutableList<FriendsFeedView>
}