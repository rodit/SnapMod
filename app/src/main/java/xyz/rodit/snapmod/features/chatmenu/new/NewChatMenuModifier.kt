package xyz.rodit.snapmod.features.chatmenu.new

import xyz.rodit.snapmod.createDelegate
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.features.chatmenu.shared.export
import xyz.rodit.snapmod.features.chatmenu.shared.previewChat
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.before

class NewChatMenuModifier(context: FeatureContext) : Feature(context) {

    private val plugins = mutableListOf<MenuPlugin>()

    override fun init() {
        registerPlain("Export") { export(context, it) }
        registerPlain("Preview") { previewChat(context, it) }

        registerSwitch("pinning", "Pin Conversation") { it.pinned }
        registerSwitch("stealth", "Stealth Mode") { it.stealth }
        registerSwitch("auto_save", "Auto-Save Messages") { it.autoSave }
        registerSwitch("auto_download", "Auto-Download Snaps") { it.autoDownload }
    }

    private fun registerPlugin(plugin: MenuPlugin) {
        plugins.add(plugin)
    }

    private fun registerPlain(text: String, click: ClickHandler) {
        registerPlugin(PlainOption(context, text, click))
    }

    private fun registerSwitch(name: String, text: String, manager: Manager) {
        registerPlugin(SwitchOption(context, name, text, manager))
    }

    override fun performHooks() {
        // Add subsection
        ProfileActionSheetCreator.apply.before {
            if (it.args[0] !is List<*>) return@before

            val newItems = (it.args[0] as List<*>).toMutableList()
            val creator = ProfileActionSheetCreator.wrap(it.thisObject)
            if (!NestedActionMenuContext.isInstance(creator.nestedContext)
                || !ActionMenuContext.isInstance(creator.actionMenuContext)) return@before

            val nestedContext = NestedActionMenuContext.wrap(creator.nestedContext)
            val actionContext = ActionMenuContext.wrap(creator.actionMenuContext)
            val key = actionContext.feedInfo.key

            val subOptions = plugins.filter(MenuPlugin::shouldCreate).map { p ->
                p.createModel(key)
            }
            val clickProxy =
                Func0.getMappedClass().createDelegate(context.classLoader) { _, _ ->
                    NestedActionMenuContext.display(
                        nestedContext,
                        "SnapMod",
                        subOptions
                    )
                    null
                }
            val snapModSettings =
                ActionClickableCaret("SnapMod Settings", null, Func0.wrap(clickProxy)).instance
            newItems.add(snapModSettings)

            it.args[0] = newItems
        }
    }
}