package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*

abstract class ButtonOption(context: FeatureContext, name: String, private val textResource: Int) :
    MenuPlugin(context, name) {

    override fun createModel(key: String?): Any {
        val actionDataModel = SendChatActionDataModel(createEventData(key!!), false, null)
        val action = SendChatAction(actionDataModel)
        val textViewModel = ActionMenuOptionTextViewModel(textResource, null, null, null, null, 62)
        return ActionMenuOptionItemViewModel(
            textViewModel,
            ActionMenuActionModel(arrayOf(action.instance)),
            0,
            null,
            null,
            null,
            null,
            false,
            ActionMenuOptionItemType.OPTION_ITEM()
        ).instance
    }
}