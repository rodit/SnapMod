package xyz.rodit.snapmod.features.chatmenu;

import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.ActionMenuActionModel;
import xyz.rodit.snapmod.mappings.ActionMenuOptionTextViewModel;
import xyz.rodit.snapmod.mappings.ActionMenuOptionToggleItemViewModel;
import xyz.rodit.snapmod.mappings.SendChatAction;
import xyz.rodit.snapmod.mappings.SendChatActionDataModel;

public abstract class ToggleOption extends MenuPlugin {

    protected final int textResource;

    public ToggleOption(FeatureContext context, String name, int textResource) {
        super(context, name);
        this.textResource = textResource;
    }

    protected abstract boolean isToggled(String key);

    @Override
    protected Object createModel(String key) {
        SendChatActionDataModel actionDataModel = new SendChatActionDataModel(createEventData(name, key), false, null);
        SendChatAction action = new SendChatAction(actionDataModel);
        ActionMenuOptionTextViewModel textViewModel = new ActionMenuOptionTextViewModel(textResource, null, null, null, null, 62);
        return new ActionMenuOptionToggleItemViewModel(textViewModel, new ActionMenuActionModel(new Object[]{action.instance}), isToggled(key)).instance;
    }
}
