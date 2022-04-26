package xyz.rodit.snapmod.util;

import xyz.rodit.snapmod.mappings.ActionMenuActionModel;
import xyz.rodit.snapmod.mappings.ActionMenuOptionTextViewModel;
import xyz.rodit.snapmod.mappings.ActionMenuOptionToggleItemViewModel;
import xyz.rodit.snapmod.mappings.SendChatAction;
import xyz.rodit.snapmod.mappings.SendChatActionDataModel;

public class ChatMenuUtil {

    public static ActionMenuOptionToggleItemViewModel createToggleOption(int textResource, String eventData, boolean toggled) {
        SendChatActionDataModel actionDataModel = new SendChatActionDataModel(eventData, false, null);
        SendChatAction action = new SendChatAction(actionDataModel);
        ActionMenuOptionTextViewModel textViewModel = new ActionMenuOptionTextViewModel(textResource, null, null, null, null, 62);
        return new ActionMenuOptionToggleItemViewModel(textViewModel, new ActionMenuActionModel(new Object[]{action.instance}), toggled);
    }
}
