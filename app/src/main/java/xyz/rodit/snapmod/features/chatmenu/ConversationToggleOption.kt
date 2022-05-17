package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.util.ConversationManager
import xyz.rodit.snapmod.util.getList

typealias Manager = (FeatureContext) -> ConversationManager

class ConversationToggleOption(
    context: FeatureContext,
    name: String,
    textResource: Int,
    private val manager: Manager
) : ToggleOption(context, name, textResource) {

    override fun shouldCreate() = !context.config.getList("hidden_chat_options").contains(name)

    override fun isToggled(key: String?) = manager(context).isEnabled(key)

    override fun handleEvent(data: String?) = manager(context).toggle(data)
}