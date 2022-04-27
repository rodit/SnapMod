package xyz.rodit.snapmod.features.conversations;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.ArroyoMessageListDataProvider;
import xyz.rodit.snapmod.mappings.ChatContext;
import xyz.rodit.snapmod.mappings.PresenceSession;

public class PreventBitmojiPresence extends StealthFeature {

    private String currentConversationId;

    public PreventBitmojiPresence(FeatureContext context) {
        super(context);
    }

    @Override
    protected void init() {
        setClass(PresenceSession.getMapping());

        putFilters(PresenceSession.activate,
                p -> null,
                p -> currentConversationId,
                new NullFilter(context, "hide_bitmoji_presence"));

        putFilters(PresenceSession.deactivate,
                p -> null,
                p -> currentConversationId,
                new NullFilter(context, "hide_bitmoji_presence"));

        putFilters(PresenceSession.processTypingActivity,
                p -> null,
                p -> currentConversationId,
                new NullFilter(context, "hide_bitmoji_presence"));
    }

    @Override
    protected void performHooks() {
        super.performHooks();

        ArroyoMessageListDataProvider.enterConversation.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                currentConversationId = ChatContext.wrap(param.args[0]).getConversationId();
            }
        });
    }
}
