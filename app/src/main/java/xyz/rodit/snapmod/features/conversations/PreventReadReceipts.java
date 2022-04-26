package xyz.rodit.snapmod.features.conversations;

import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.ConversationManager;
import xyz.rodit.snapmod.util.UUIDUtil;

public class PreventReadReceipts extends StealthFeature {

    public PreventReadReceipts(FeatureContext context) {
        super(context);
    }

    @Override
    protected void init() {
        setClass(ConversationManager.getMapping());

        putFilters(ConversationManager.displayedMessages,
                p -> null,
                p -> UUIDUtil.fromSnap(p.args[0]),
                new ObjectFilter<>(context, "hide_read", (Object) null));
    }
}
