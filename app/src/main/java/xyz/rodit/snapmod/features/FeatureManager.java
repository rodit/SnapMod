package xyz.rodit.snapmod.features;

import java.util.ArrayList;
import java.util.List;

import xyz.rodit.snapmod.features.chatmenu.ChatMenuModifier;
import xyz.rodit.snapmod.features.conversations.MessageInterceptor;
import xyz.rodit.snapmod.features.conversations.PreventBitmojiPresence;
import xyz.rodit.snapmod.features.conversations.PreventReadReceipts;
import xyz.rodit.snapmod.features.conversations.PreventTypingNotifications;
import xyz.rodit.snapmod.features.conversations.SnapInteractionFilter;
import xyz.rodit.snapmod.features.conversations.SnapOverrides;
import xyz.rodit.snapmod.features.friendsfeed.FeedModifier;
import xyz.rodit.snapmod.features.info.AdditionalFriendInfo;
import xyz.rodit.snapmod.features.info.NetworkLogging;
import xyz.rodit.snapmod.features.opera.OperaModelModifier;
import xyz.rodit.snapmod.features.saving.ChatSaving;
import xyz.rodit.snapmod.features.saving.PublicProfileSaving;
import xyz.rodit.snapmod.features.saving.StoriesSaving;
import xyz.rodit.snapmod.features.tweaks.BypassVideoLength;
import xyz.rodit.snapmod.features.tweaks.ConfigurationTweaks;
import xyz.rodit.snapmod.features.tweaks.DisableBitmojis;
import xyz.rodit.snapmod.features.tweaks.HideFriends;
import xyz.rodit.snapmod.features.tweaks.HideStoryReadReceipts;
import xyz.rodit.snapmod.features.tweaks.HideStorySections;
import xyz.rodit.snapmod.features.tweaks.LocationOverride;

public class FeatureManager extends Contextual {

    private final List<Feature> features = new ArrayList<>();

    public FeatureManager(FeatureContext context) {
        super(context);
    }

    public void load() {
        // Chat context menu
        add(ChatMenuModifier::new);

        // Friends feed
        add(FeedModifier::new);

        // Conversations/chats
        add(MessageInterceptor::new);
        add(PreventBitmojiPresence::new);
        add(PreventReadReceipts::new);
        add(PreventTypingNotifications::new);
        add(SnapInteractionFilter::new);
        add(SnapOverrides::new);

        // Opera (story/snap view)
        add(OperaModelModifier::new);

        // Saving
        add(ChatSaving::new);
        add(PublicProfileSaving::new);
        add(StoriesSaving::new);

        // Information
        add(AdditionalFriendInfo::new);
        add(NetworkLogging::new);

        // Tweaks
        add(BypassVideoLength::new);
        add(ConfigurationTweaks::new);
        add(DisableBitmojis::new);
        // add(FriendAddOverride::new);
        add(HideFriends::new);
        add(HideStoryReadReceipts::new);
        add(HideStorySections::new);
        add(LocationOverride::new);
    }

    public void init() {
        features.forEach(Feature::init);
    }

    public void onConfigLoaded(boolean first) {
        features.forEach(f -> f.onConfigLoaded(first));
    }

    public void performHooks() {
        features.forEach(Feature::performHooks);
    }

    public void add(FeatureSupplier supplier) {
        features.add(supplier.supply(context));
    }

    @FunctionalInterface
    public interface FeatureSupplier {

        Feature supply(FeatureContext context);
    }
}
