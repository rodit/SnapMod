package xyz.rodit.snapmod.features.opera

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.MapKey
import xyz.rodit.snapmod.mappings.ParamsMap

class OperaModelModifier(context: FeatureContext) : Feature(context) {

    private val plugins: MutableList<OperaPlugin> = ArrayList()

    override fun init() {
        plugins.add(MenuModifier(context))
        plugins.add(SnapDurationModifier(context))
    }

    override fun performHooks() {
        // Modify opera model map on insert.
        ParamsMap.put.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val key = MapKey.wrap(param.args[0]).name
                plugins.filter { it.isEnabled && it.shouldOverride(key) }
                    .forEach { param.args[1] = it.override(key, param.args[1]) }
            }
        })
    }
}