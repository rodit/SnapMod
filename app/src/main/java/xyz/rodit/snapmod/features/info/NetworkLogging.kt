package xyz.rodit.snapmod.features.info

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.NetworkApi
import xyz.rodit.snapmod.util.before

class NetworkLogging(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Hook network manager to log requests.
        val hook = { it: XC_MethodHook.MethodHookParam -> XposedBridge.log(it.args[0].toString()) }

        NetworkApi.submit.before(context, "log_network_requests", hook)
    }
}