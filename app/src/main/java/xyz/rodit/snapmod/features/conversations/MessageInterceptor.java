package xyz.rodit.snapmod.features.conversations;

import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.ContentType;
import xyz.rodit.snapmod.mappings.ConversationManager;
import xyz.rodit.snapmod.mappings.LocalMessageContent;
import xyz.rodit.snapmod.mappings.MessageDestinations;
import xyz.rodit.snapmod.mappings.MessageUpdate;
import xyz.rodit.snapmod.util.UUIDUtil;

public class MessageInterceptor extends StealthFeature {

    public MessageInterceptor(FeatureContext context) {
        super(context);
    }

    @Override
    protected void init() {
        setClass(ConversationManager.getMapping());

        putFilters(ConversationManager.sendMessageWithContent,
                p -> LocalMessageContent.wrap(p.args[1]).getContentType(),
                p -> UUIDUtil.fromSnap(MessageDestinations.wrap(p.args[0]).getConversations().get(0)),
                new ObjectFilter<>(context, "hide_screenshot", ContentType.STATUS_CONVERSATION_CAPTURE_SCREENSHOT(), ContentType.STATUS_CONVERSATION_CAPTURE_RECORD()),
                new ObjectFilter<>(context, "hide_save_gallery", ContentType.STATUS_SAVE_TO_CAMERA_ROLL()));

        putFilters(ConversationManager.updateMessage,
                p -> MessageUpdate.wrap(p.args[2]),
                p -> UUIDUtil.fromSnap(p.args[0]),
                new ObjectFilter<>(context, "hide_read", MessageUpdate.READ()),
                new ObjectFilter<>(context, "hide_save", MessageUpdate.SAVE(), MessageUpdate.UNSAVE()),
                new ObjectFilter<>(context, "hide_screenshot", MessageUpdate.SCREENSHOT(), MessageUpdate.SCREEN_RECORD()),
                new ObjectFilter<>(context, "hide_replay", MessageUpdate.REPLAY()),
                new ObjectFilter<>(context, "dont_release", MessageUpdate.RELEASE()));
    }
}