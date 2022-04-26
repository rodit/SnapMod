package xyz.rodit.snapmod.features.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.BitmojiUriHandler;

public class DisableBitmojis extends Feature {

    public DisableBitmojis(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Disable Bitmoji avatars.
        BitmojiUriHandler.handle.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("disable_bitmojis")) {
                    param.setResult(null);
                }
            }
        });
    }
}
