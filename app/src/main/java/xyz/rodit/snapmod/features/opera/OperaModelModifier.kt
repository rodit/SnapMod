package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.MapKey
import xyz.rodit.snapmod.mappings.ParamsMap
import xyz.rodit.snapmod.util.before

class OperaModelModifier(context: FeatureContext) : Feature(context) {

    private val plugins: MutableList<OperaPlugin> = ArrayList()

    override fun init() {
        plugins.add(MenuModifier(context))
        plugins.add(SnapDurationModifier(context))
    }

    override fun performHooks() {
        // Modify opera model map on insert.
        ParamsMap.put.before {
            val params = ParamsMap.wrap(it.thisObject)
            val key = MapKey.wrap(it.args[0]).name
            plugins.filter { p -> p.isEnabled && p.shouldOverride(params, key) }
                .forEach { p -> it.args[1] = p.override(params, key, it.args[1]) }
        }
    }
}