package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel
import xyz.rodit.snapmod.mappings.ParamsMap

interface MenuPlugin {

    val isEnabled: Boolean
    fun createActions(params: ParamsMap): Collection<OperaActionMenuOptionViewModel>
}