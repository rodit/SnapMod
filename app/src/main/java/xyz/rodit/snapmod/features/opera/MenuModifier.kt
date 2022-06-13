package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel
import xyz.rodit.snapmod.mappings.ParamsMap

private const val KEY_NAME = "action_menu_options"

class MenuModifier(context: FeatureContext) : Contextual(context), OperaPlugin {

    private val plugins: MutableList<MenuPlugin> = ArrayList()

    override val isEnabled: Boolean
        get() {
            return true
        }

    override fun shouldOverride(params: ParamsMap, key: String): Boolean {
        return KEY_NAME == key
    }

    override fun override(params: ParamsMap, key: String, value: Any): Any {
        if (value !is List<*>) return value
        val newList: MutableList<Any> = ArrayList()
        newList.addAll(value.filterNotNull())
        newList.addAll(
            plugins.filter(MenuPlugin::isEnabled)
                .flatMap { it.createActions(params) }
                .map(OperaActionMenuOptionViewModel::instance)
        )
        return newList
    }

    init {
        plugins.add(SaveMenuOption(context))
        plugins.add(context.features.get<CustomStoryOptions>())
    }
}