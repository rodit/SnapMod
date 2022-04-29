package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel

interface MenuPlugin {

    val isEnabled: Boolean
    fun createActions(): Collection<OperaActionMenuOptionViewModel>
}