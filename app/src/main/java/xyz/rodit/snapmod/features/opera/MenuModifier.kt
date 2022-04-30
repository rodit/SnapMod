package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel

private const val KEY_NAME = "action_menu_options"

class MenuModifier(context: FeatureContext) : Contextual(context), OperaPlugin {

    private val plugins: MutableList<MenuPlugin> = ArrayList()

    override val isEnabled: Boolean
        get() {
            return true
        }

    override fun shouldOverride(key: String): Boolean {
        return KEY_NAME == key
    }

    override fun override(key: String, value: Any): Any {
        if (value !is List<*>) return value
        val newList: MutableList<Any> = ArrayList()
        newList.addAll(value.filterNotNull())
        newList.addAll(
            plugins.filter(MenuPlugin::isEnabled)
                .flatMap(MenuPlugin::createActions)
                .map(OperaActionMenuOptionViewModel::instance)
        )
        return newList
    }

    init {
        plugins.add(SaveMenuOption(context))
    }
}