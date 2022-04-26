package xyz.rodit.snapmod.features.tweaks;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.ComposerFriend;
import xyz.rodit.snapmod.mappings.DisplayInfoContainer;
import xyz.rodit.snapmod.mappings.FriendListener;
import xyz.rodit.snapmod.mappings.ProfileMyFriendsSection;

public class HideFriends extends Feature {

    private final Set<String> hiddenFriends = new HashSet<>();

    public HideFriends(FeatureContext context) {
        super(context);
    }

    @Override
    protected void onConfigLoaded(boolean first) {
        hiddenFriends.clear();
        for (String username : context.config.getString("hidden_friends", "").split("\n")) {
            if (!TextUtils.isEmpty(username)) {
                hiddenFriends.add(username.trim());
            }
        }
    }

    @Override
    protected void performHooks() {
        // Hide friends from 'My Friends' in profile.
        ProfileMyFriendsSection.filter.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("hide_friends")) {
                    List list = (List) param.args[0];
                    List filtered = new ArrayList();
                    for (Object o : list) {
                        String term = DisplayInfoContainer.wrap(o).getTerm();
                        if (!hiddenFriends.contains(term)) {
                            filtered.add(o);
                        }
                    }

                    param.args[0] = filtered;
                }
            }
        });

        // Hide friends from best friends list.
        FriendListener.handle.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("hide_friends") && param.args[0] instanceof List) {
                    List list = (List) param.args[0];
                    if (list.isEmpty() || !ComposerFriend.isInstance(list.get(0))) {
                        return;
                    }

                    List filtered = new ArrayList();
                    for (Object o : list) {
                        String username = ComposerFriend.wrap(o).getUser().getUsername();
                        if (!hiddenFriends.contains(username)) {
                            filtered.add(o);
                        }
                    }

                    param.args[0] = filtered;
                }
            }
        });
    }
}
