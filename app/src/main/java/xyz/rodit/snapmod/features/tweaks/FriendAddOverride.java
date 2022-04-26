package xyz.rodit.snapmod.features.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.FriendActionClient;
import xyz.rodit.snapmod.mappings.FriendActionRequest;

public class FriendAddOverride extends Feature {

    public FriendAddOverride(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Override friend add method (not sure if this works).
        FriendActionClient.sendFriendAction.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("enable_friend_override")) {
                    FriendActionRequest request = FriendActionRequest.wrap(param.args[0]);
                    if (request.getAction().equals("add")) {
                        String addMethod = context.config.getString("friend_override_value", "ADDED_BY_USERNAME");
                        request.setAddedBy(addMethod);
                    }
                }
            }
        });
    }
}
