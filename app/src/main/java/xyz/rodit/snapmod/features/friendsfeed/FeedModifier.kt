package xyz.rodit.snapmod.features.friendsfeed

import xyz.rodit.snapmod.ObjectProxy
import xyz.rodit.snapmod.Shared
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.FriendsFeedRecordHolder
import xyz.rodit.snapmod.mappings.FriendsFeedView
import xyz.rodit.snapmod.mappings.SnapIterable
import xyz.rodit.snapmod.util.after
import java.lang.reflect.Proxy

class FeedModifier(context: FeatureContext) : Feature(context) {

    private val sorters: MutableList<FeedSorter> = ArrayList()

    override fun init() {
        sorters.add(PinSorter(context))
    }

    override fun performHooks() {
        // Hook feed list and apply re-ordering.
        FriendsFeedRecordHolder.constructors.after {
            val self = FriendsFeedRecordHolder.wrap(it.thisObject)
            self.emojis.map[Shared.PINNED_FRIENDMOJI_NAME] = Shared.PINNED_FRIENDMOJI_EMOJI

            var views: MutableList<FriendsFeedView> =
                (self.records.instance as Iterable<*>).map(FriendsFeedView::wrap).toMutableList()

            sorters.filter(FeedSorter::shouldApply).forEach { s -> views = s.sort(views) }

            val sorted = views.map(FriendsFeedView::instance)
            val iterableProxy = Proxy.newProxyInstance(
                context.classLoader,
                arrayOf(SnapIterable.getMappedClass()),
                ObjectProxy(sorted)
            )

            self.records = SnapIterable.wrap(iterableProxy)
        }
    }
}
