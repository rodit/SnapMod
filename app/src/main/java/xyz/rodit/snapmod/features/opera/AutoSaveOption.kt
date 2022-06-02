package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.CustomResources
import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.features.saving.storyId
import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel
import xyz.rodit.snapmod.mappings.OperaContextAction
import xyz.rodit.snapmod.mappings.ParamsMap

class AutoSaveOption(context: FeatureContext) : Contextual(context), MenuPlugin {

    override val isEnabled = true

    override fun createActions(params: ParamsMap): Collection<OperaActionMenuOptionViewModel> {
        return setOf(
            OperaActionMenuOptionViewModel(
                0,
                if (context.autoDownloadStories.isEnabled(params.storyId))
                    CustomResources.string.menu_story_disable_auto_download
                else
                    CustomResources.string.menu_story_enable_auto_download,
                true,
                OperaContextAction.HIDE_AD()
            )
        )
    }
}