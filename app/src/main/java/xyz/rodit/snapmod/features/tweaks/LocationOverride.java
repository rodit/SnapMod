package xyz.rodit.snapmod.features.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.LocationMessage;
import xyz.rodit.snapmod.mappings.LocationMessageBuilder;

public class LocationOverride extends Feature {

    public LocationOverride(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Override location (not sure if this works)
        LocationMessageBuilder.transform.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("location_share_override")) {
                    double lat = Double.parseDouble(context.config.getString("location_share_lat", "0"));
                    double $long = Double.parseDouble(context.config.getString("location_share_long", "0"));
                    LocationMessage loc = LocationMessage.wrap(param.args[0]);
                    loc.setLatitude(lat);
                    loc.setLongitude($long);
                }
            }
        });
    }
}
