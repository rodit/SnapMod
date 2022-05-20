package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.CustomResources.string.menu_option_preview
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.features.chatmenu.shared.previewChat

class PreviewOption(context: FeatureContext) :
    ButtonOption(context, "preview", menu_option_preview) {

    override fun shouldCreate() = true

    override fun handleEvent(data: String?) {
        if (data == null) return

        previewChat(context, data)
    }
}