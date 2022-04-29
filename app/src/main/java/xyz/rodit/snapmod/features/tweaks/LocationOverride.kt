package xyz.rodit.snapmod.features.tweaks

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.LocationMessage
import xyz.rodit.snapmod.mappings.LocationMessageBuilder

class LocationOverride(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Override location (not sure if this works).
        LocationMessageBuilder.transform.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (context.config.getBoolean("location_share_override")) {
                    val lat = context.config.getString("location_share_lat", "0").toDouble()
                    val long = context.config.getString("location_share_long", "0").toDouble()
                    val loc = LocationMessage.wrap(param.args[0])
                    loc.latitude = lat
                    loc.longitude = long
                }
            }
        })
    }
}