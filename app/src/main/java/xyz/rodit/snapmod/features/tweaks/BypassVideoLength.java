package xyz.rodit.snapmod.features.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.MemoriesPickerVideoDurationConfig;

public class BypassVideoLength extends Feature {

    public BypassVideoLength(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Bypass video duration limit from gallery in chat.
        MemoriesPickerVideoDurationConfig.constructors.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("bypass_video_length_restrictions")) {
                    param.args[0] = Long.MAX_VALUE;
                }
            }
        });
    }
}
