package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel
import xyz.rodit.snapmod.mappings.OperaContextActions
import xyz.rodit.snapmod.mappings.ParamsMap

class SaveMenuOption(context: FeatureContext) : Contextual(context), MenuPlugin {

    override val isEnabled: Boolean
        get() = context.config.getBoolean("allow_download_stories")

    override fun createActions(params: ParamsMap): Collection<OperaActionMenuOptionViewModel> {
        return setOf(OperaContextActions.getSaveAction())
    }
}