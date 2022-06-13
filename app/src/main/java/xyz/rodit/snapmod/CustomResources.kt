package xyz.rodit.snapmod

import android.content.Context
import android.content.res.Resources
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object CustomResources {

    private val strings: MutableMap<Int, String> = mutableMapOf(
        string.menu_option_stealth_mode to "Stealth Mode",
        string.menu_option_preview to "More Information",
        string.menu_option_auto_save to "Auto-Save Messages",
        string.menu_option_auto_download to "Auto-Download Snaps",
        string.menu_option_export to "Export...",

        string.chat_action_playback_speed to "Set Playback Speed",

        string.menu_story_custom_options to "SnapMod:CUSTOM_STORY_OPTIONS"
    )

    fun init() {
        XposedBridge.hookAllMethods(Resources::class.java, "getString", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val id = param.args[0] as Int
                strings[id]?.let {
                    param.result = it
                }
            }
        })

        XposedBridge.hookAllMethods(Context::class.java, "getString", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val id = param.args[0] as Int
                strings[id]?.let {
                    param.result = it
                }
            }
        })
    }

    fun get(id: Int): String? {
        return strings[id]
    }

    object string {
        const val menu_option_stealth_mode = -100000
        const val menu_option_preview = -100001
        const val menu_option_auto_save = -100002
        const val menu_option_auto_download = -100003
        const val menu_option_export = -100004

        const val chat_action_playback_speed = -200000

        const val menu_story_custom_options = -300000
    }
}