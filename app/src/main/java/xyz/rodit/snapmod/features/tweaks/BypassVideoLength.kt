package xyz.rodit.snapmod.features.tweaks

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.MemoriesPickerVideoDurationConfig

class BypassVideoLength(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Bypass video duration limit from gallery in chat.
        MemoriesPickerVideoDurationConfig.constructors.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("bypass_video_length_restrictions")) {
                    param.args[0] = Long.MAX_VALUE
                }
            }
        })
    }
}