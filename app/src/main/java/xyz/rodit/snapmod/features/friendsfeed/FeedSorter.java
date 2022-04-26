package xyz.rodit.snapmod.features.friendsfeed;

import java.util.List;

import xyz.rodit.snapmod.features.Contextual;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.FriendsFeedView;

public abstract class FeedSorter extends Contextual {

    public FeedSorter(FeatureContext context) {
        super(context);
    }

    abstract boolean shouldApply();

    abstract List<FriendsFeedView> sort(List<FriendsFeedView> items);
}
