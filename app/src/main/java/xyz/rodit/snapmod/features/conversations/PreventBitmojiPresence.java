package xyz.rodit.snapmod.features.conversations;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.Callback;
import xyz.rodit.snapmod.mappings.ConversationManager;
import xyz.rodit.snapmod.util.UUIDUtil;

public class PreventBitmojiPresence extends StealthFeature {

    public PreventBitmojiPresence(FeatureContext context) {
        super(context);
    }

    @Override
    protected void init() {
        setClass(ConversationManager.getMapping());

        putFilters(ConversationManager.enterConversation,
                p -> null,
                p -> UUIDUtil.fromSnap(p.args[0]),
                new NullFilter(context, "hide_enter_conversation"));

        putFilters(ConversationManager.exitConversation,
                p -> null,
                p -> UUIDUtil.fromSnap(p.args[0]),
                new NullFilter(context, "hide_exit_conversation"));

        putFilters(ConversationManager.sendTypingNotification,
                p -> null,
                p -> UUIDUtil.fromSnap(p.args[0]),
                new NullFilter(context, "hide_typing"));
    }

    @Override
    protected void onPostHook(XC_MethodHook.MethodHookParam param) {
        for (Object o : param.args) {
            if (Callback.isInstance(o)) {
                Callback.wrap(o).onSuccess();
            }
        }
    }
}
