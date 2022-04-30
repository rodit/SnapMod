package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*

abstract class ToggleOption(context: FeatureContext, name: String, private val textResource: Int) : MenuPlugin(context, name) {

    protected abstract fun isToggled(key: String?): Boolean

    override fun createModel(key: String?): Any {
        val actionDataModel = SendChatActionDataModel(createEventData(key!!), false, null)
        val action = SendChatAction(actionDataModel)
        val textViewModel = ActionMenuOptionTextViewModel(textResource, null, null, null, null, 62)
        return ActionMenuOptionToggleItemViewModel(
            textViewModel,
            ActionMenuActionModel(arrayOf(action.instance)),
            isToggled(key)
        ).instance
    }
}