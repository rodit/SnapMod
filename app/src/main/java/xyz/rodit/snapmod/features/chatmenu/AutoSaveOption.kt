package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.CustomResources
import xyz.rodit.snapmod.features.FeatureContext

class AutoSaveOption(context: FeatureContext) :
    ToggleOption(context, "auto_save", CustomResources.string.menu_option_auto_save) {

    override fun shouldCreate(): Boolean {
        return true
    }

    override fun handleEvent(key: String?) {
        context.autoSave.toggle(key)
    }

    override fun isToggled(key: String?): Boolean {
        return context.autoSave.isEnabled(key)
    }
}