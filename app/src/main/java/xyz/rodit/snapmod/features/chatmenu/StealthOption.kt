package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.CustomResources
import xyz.rodit.snapmod.features.FeatureContext

class StealthOption(context: FeatureContext) :
    ToggleOption(context, "stealth", CustomResources.string.menu_option_stealth_mode) {

    override fun shouldCreate(): Boolean {
        return true
    }

    override fun handleEvent(key: String?) {
        context.stealth.toggle(key)
    }

    override fun isToggled(key: String?): Boolean {
        return context.stealth.isEnabled(key)
    }
}