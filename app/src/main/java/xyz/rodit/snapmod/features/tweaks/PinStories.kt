package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.ObjectProxy
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.before
import xyz.rodit.snapmod.util.pairSecond
import java.lang.reflect.Proxy

class PinStories(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        DfSectionControllerActions.apply.before {
            if (!DfSectionController.isInstance(DfSectionControllerActions.wrap(it.thisObject).sectionController)) return@before

            val pairSecond = it.args[0].pairSecond
            if (!DataWithPagination.isInstance(pairSecond)) return@before

            val data = DataWithPagination.wrap(pairSecond)
            val dataModels = (data.dataModels.instance as Iterable<*>).toList()
            if (dataModels.isEmpty()) return@before

            val reordered =
                dataModels.sortedByDescending { model ->
                    if (!StoryViewModel.isInstance(model)) return@sortedByDescending 0

                    val storyData = StoryViewModel.wrap(model).storyData.instance
                    if (!FriendStoryData.isInstance(storyData)) return@sortedByDescending 0

                    val friendStoryData = FriendStoryData.wrap(storyData)
                    if (context.pinnedStories.isEnabled(friendStoryData.storyRecordStoryId)) {
                        val displayName = friendStoryData.displayName
                        if (!displayName.startsWith("\uD83D\uDCCC")) {
                            friendStoryData.displayName = "\uD83D\uDCCC $displayName"
                        }
                        1
                    } else 0
                }
            val iterableProxy = Proxy.newProxyInstance(
                context.classLoader,
                arrayOf(SnapIterable.getMappedClass()),
                ObjectProxy(reordered)
            )

            data.dataModels = SnapIterable.wrap(iterableProxy)
        }
    }
}