package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.CustomResources.string.menu_option_export
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.features.chatmenu.shared.export

class ExportOption(context: FeatureContext):
    ButtonOption(context, "export_chat", menu_option_export) {

    override fun shouldCreate() = true

    override fun handleEvent(data: String?) {
        if (data == null) return

        export(context, data)
    }
}