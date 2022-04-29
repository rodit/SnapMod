package xyz.rodit.snapmod.features.tweaks

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.RxSupplier
import xyz.rodit.snapmod.mappings.UploadSnapReadReceiptDurableJobProcessor

class HideStoryReadReceipts(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Prevent story read receipt uploads.
        UploadSnapReadReceiptDurableJobProcessor.uploadReadReceipts.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("hide_story_views")) {
                    param.result = RxSupplier.supplyNothing().instance
                }
            }
        })
    }
}
