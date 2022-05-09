package xyz.rodit.snapmod.features.messagemenu

import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ChatMenuItem
import xyz.rodit.snapmod.mappings.ChatModelBase

abstract class MenuPlugin(context: FeatureContext) : Contextual(context) {

    abstract fun isEnabled(): Boolean

    abstract fun createOptions(model: ChatModelBase): List<ChatMenuItem>

    open fun performHooks() {

    }
}