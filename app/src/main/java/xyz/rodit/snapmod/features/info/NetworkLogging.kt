package xyz.rodit.snapmod.features.info

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.NetworkApi

class NetworkLogging(context: FeatureContext) : Feature(context) {
    override fun performHooks() {
        // Hook network manager to log requests.
        val networkHook: XC_MethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("log_network_requests")) {
                    XposedBridge.log(param.args[0].toString())
                }
            }
        }

        NetworkApi.submit.hook(networkHook)
        NetworkApi.submitToNetworkManagerDirectly.hook(networkHook)
    }
}