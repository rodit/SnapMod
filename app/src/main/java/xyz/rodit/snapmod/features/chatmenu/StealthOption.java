package xyz.rodit.snapmod.features.chatmenu;

import xyz.rodit.snapmod.CustomResources;
import xyz.rodit.snapmod.features.FeatureContext;

public class StealthOption extends ToggleOption {

    public StealthOption(FeatureContext context) {
        super(context, "stealth", CustomResources.string.menu_option_stealth_mode);
    }

    @Override
    protected boolean shouldCreate() {
        return true;
    }

    @Override
    protected void handleEvent(String key) {
        context.stealth.toggle(key);
    }

    @Override
    protected boolean isToggled(String key) {
        return context.stealth.isEnabled(key);
    }
}