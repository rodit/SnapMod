package xyz.rodit.snapmod.features.info;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.NetworkApi;

public class NetworkLogging extends Feature {

    public NetworkLogging(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Hook network manager to log requests.
        XC_MethodHook networkHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("log_network_requests")) {
                    XposedBridge.log(param.args[0].toString());
                }
            }
        };

        NetworkApi.submit.hook(networkHook);
        NetworkApi.submitToNetworkManagerDirectly.hook(networkHook);
    }
}
