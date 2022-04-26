package xyz.rodit.snapmod.features.conversations;

import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.SnapInteractionType;
import xyz.rodit.snapmod.mappings.SnapManager;
import xyz.rodit.snapmod.util.UUIDUtil;

public class SnapInteractionFilter extends StealthFeature {

    public SnapInteractionFilter(FeatureContext context) {
        super(context);
    }

    @Override
    protected void init() {
        setClass(SnapManager.getMapping());

        putFilters(SnapManager.onSnapInteraction,
                p -> SnapInteractionType.wrap(p.args[0]),
                p -> UUIDUtil.fromSnap(p.args[1]),
                new ObjectFilter<>(context, "hide_snap_views", SnapInteractionType.VIEWING_INITIATED(), SnapInteractionType.VIEWING_FINISHED(), SnapInteractionType.MARK_AS_INVALID()));
    }
}
