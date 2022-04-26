package xyz.rodit.snapmod.features.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.RxSupplier;
import xyz.rodit.snapmod.mappings.UploadSnapReadReceiptDurableJobProcessor;

public class HideStoryReadReceipts extends Feature {

    public HideStoryReadReceipts(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Prevent story read receipt uploads.
        UploadSnapReadReceiptDurableJobProcessor.uploadReadReceipts.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("hide_story_views")) {
                    param.setResult(RxSupplier.supplyNothing().instance);
                }
            }
        });
    }
}
