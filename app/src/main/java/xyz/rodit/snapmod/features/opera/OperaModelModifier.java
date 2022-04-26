package xyz.rodit.snapmod.features.opera;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.MapKey;
import xyz.rodit.snapmod.mappings.ParamsMap;

public class OperaModelModifier extends Feature {

    private final List<OperaPlugin> plugins = new ArrayList<>();

    public OperaModelModifier(FeatureContext context) {
        super(context);
    }

    @Override
    protected void init() {
        plugins.add(new MenuModifier(context));
        plugins.add(new SnapDurationModifier(context));
    }

    @Override
    protected void performHooks() {
        // Modify opera model map on insert.
        ParamsMap.put.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String key = MapKey.wrap(param.args[0]).getName();
                plugins.stream().filter(p -> p.isEnabled() && p.shouldOverride(key)).forEach(
                        p -> param.args[1] = p.override(key, param.args[1])
                );
            }
        });
    }
}
