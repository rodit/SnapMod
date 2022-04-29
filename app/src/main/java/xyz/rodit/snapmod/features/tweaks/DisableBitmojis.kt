package xyz.rodit.snapmod.features.tweaks

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.BitmojiUriHandler

class DisableBitmojis(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Disable Bitmoji avatars.
        BitmojiUriHandler.handle.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("disable_bitmojis")) {
                    param.result = null
                }
            }
        })
    }
}