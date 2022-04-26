package xyz.rodit.snapmod.features.friendsfeed;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.FriendsFeedView;

public class PinSorter extends FeedSorter {

    public PinSorter(FeatureContext context) {
        super(context);
    }

    @Override
    protected boolean shouldApply() {
        return context.config.getBoolean("allow_pin_chats");
    }

    @Override
    protected List<FriendsFeedView> sort(List<FriendsFeedView> items) {
        List<FriendsFeedView> pinned = new ArrayList<>();
        List<FriendsFeedView> normal = new ArrayList<>();
        for (FriendsFeedView view : items) {
            if (context.pinned.isEnabled(view.getKey())) {
                String friendmojis = view.getFriendmojiCategories();
                if (!friendmojis.contains("pinned")) {
                    friendmojis = TextUtils.isEmpty(friendmojis) ? "pinned" : friendmojis + ",pinned";
                    view.setFriendmojiCategories(friendmojis);
                }

                pinned.add(view);
            } else {
                normal.add(view);
            }
        }

        List<FriendsFeedView> all = new ArrayList<>();
        all.addAll(pinned);
        all.addAll(normal);
        return all;
    }
}
