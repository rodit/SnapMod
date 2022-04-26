package xyz.rodit.snapmod.features.chatmenu;

import xyz.rodit.snapmod.Shared;
import xyz.rodit.snapmod.features.FeatureContext;

public class PinOption extends ToggleOption {

    private static final String PIN_STRING_NAME = "action_menu_pin_conversation";

    public PinOption(FeatureContext context) {
        super(context, "pinning", context.appContext.getResources().getIdentifier(PIN_STRING_NAME, "string", Shared.SNAPCHAT_PACKAGE));
    }

    @Override
    protected boolean shouldCreate() {
        return context.config.getBoolean("allow_pin_chats");
    }

    @Override
    protected void handleEvent(String key) {
        context.pinned.toggle(key);
    }

    @Override
    protected boolean isToggled(String key) {
        return context.pinned.isEnabled(key);
    }
}
