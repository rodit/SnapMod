package xyz.rodit.snapmod.features.friendsfeed

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.ObjectProxy
import xyz.rodit.snapmod.Shared
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.FriendsFeedRecordHolder
import xyz.rodit.snapmod.mappings.FriendsFeedView
import xyz.rodit.snapmod.mappings.SnapIterable
import java.lang.reflect.Proxy

class FeedModifier(context: FeatureContext) : Feature(context) {

    private val sorters: MutableList<FeedSorter> = ArrayList()

    override fun init() {
        sorters.add(PinSorter(context))
    }

    override fun performHooks() {
        // Hook feed list and apply re-ordering.
        FriendsFeedRecordHolder.constructors.hook(object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val self = FriendsFeedRecordHolder.wrap(param.thisObject)
                self.emojis.map[Shared.PINNED_FRIENDMOJI_NAME] = Shared.PINNED_FRIENDMOJI_EMOJI

                var views: MutableList<FriendsFeedView> =
                    (self.records.instance as Iterable<*>).map { FriendsFeedView.wrap(it) }
                        .toMutableList()

                sorters.filter { it.shouldApply() }.forEach { views = it.sort(views) }

                val sorted = views.map { it.instance }.toList()
                val iterableProxy = Proxy.newProxyInstance(
                    context.classLoader,
                    arrayOf(SnapIterable.getMappedClass()),
                    ObjectProxy(sorted)
                )

                self.records = SnapIterable.wrap(iterableProxy)
            }
        })
    }
}