package xyz.rodit.snapmod.util

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.dexsearch.client.xposed.HookableBase
import xyz.rodit.snapmod.features.FeatureContext

typealias FunctionHook = (param: XC_MethodHook.MethodHookParam) -> Unit

fun HookableBase.before(hook: FunctionHook) {
    hook(object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            hook(param)
        }
    })
}

fun HookableBase.before(context: FeatureContext, configKey: String, hook: FunctionHook) {
    hook(object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            if (context.config.getBoolean(configKey)) {
                hook(param)
            }
        }
    })
}

fun HookableBase.after(hook: FunctionHook) {
    hook(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            hook(param)
        }
    })
}

fun HookableBase.after(context: FeatureContext, configKey: String, hook: FunctionHook) {
    hook(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            if (context.config.getBoolean(configKey)) {
                hook(param)
            }
        }
    })
}