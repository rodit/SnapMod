package xyz.rodit.snapmod

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object CustomResources {
    private val CUSTOM_STRINGS: MutableMap<Int, String> = HashMap()

    private fun putString(key: Int, value: String) {
        CUSTOM_STRINGS[key] = value
    }

    fun init() {
        XposedBridge.hookAllMethods(Context::class.java, "getString", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val id = param.args[0] as Int
                CUSTOM_STRINGS[id]?.let {
                    param.result = it
                }
            }
        })
    }

    object string {
        const val menu_option_stealth_mode = -100000
    }

    init {
        putString(string.menu_option_stealth_mode, "Stealth Mode")
    }
}