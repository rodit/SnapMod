package xyz.rodit.snapmod.features.saving

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.MessageStoryKeys
import xyz.rodit.snapmod.mappings.OperaDisplayState
import xyz.rodit.snapmod.mappings.OperaPageViewController
import xyz.rodit.snapmod.mappings.ParamsMap
import xyz.rodit.snapmod.util.PathManager
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.toMax

class AutoDownloadStories(context: FeatureContext) : Feature(context, 84608.toMax()) {

    override fun performHooks() {
        OperaPageViewController.onDisplayStateChanged.after {
            val viewController = OperaPageViewController.wrap(it.thisObject)
            if (viewController.state.instance != OperaDisplayState.FULLY_DISPLAYED().instance) return@after

            val params = ParamsMap.wrap(viewController.metadata.instance)
            val map = params.map
            if (map.containsKey(MessageStoryKeys.getSnapInSavedState().instance)) return@after

            if (!context.config.getBoolean("auto_download_stories")) return@after

            getMediaInfo(context, params) { info ->
                downloadOperaMedia(
                    context,
                    PathManager.DOWNLOAD_STORY,
                    info
                )
            }
        }
    }
}