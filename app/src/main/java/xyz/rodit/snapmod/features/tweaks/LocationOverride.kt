package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.LocationMessage
import xyz.rodit.snapmod.mappings.LocationMessageBuilder
import xyz.rodit.snapmod.util.before

class LocationOverride(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Override location (not sure if this works).
        LocationMessageBuilder.transform.before(context, "location_share_override") {
            val lat = context.config.getString("location_share_lat", "0").toDouble()
            val long = context.config.getString("location_share_long", "0").toDouble()
            val loc = LocationMessage.wrap(it.args[0])
            loc.latitude = lat
            loc.longitude = long
        }
    }
}