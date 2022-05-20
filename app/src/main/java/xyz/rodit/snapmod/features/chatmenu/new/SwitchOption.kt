package xyz.rodit.snapmod.features.chatmenu.new

import xyz.rodit.snapmod.createDelegate
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ActionSwitch
import xyz.rodit.snapmod.mappings.Func1
import xyz.rodit.snapmod.util.ConversationManager
import xyz.rodit.snapmod.util.getList

typealias Manager = (FeatureContext) -> ConversationManager

class SwitchOption(
    private val context: FeatureContext,
    private val name: String,
    private val text: String,
    private val manager: Manager
) : MenuPlugin() {

    override fun shouldCreate() = !context.config.getList("hidden_chat_options").contains(name)

    override fun createModel(key: String): Any = ActionSwitch(
        text,
        manager(context).isEnabled(key),
        Func1.wrap(Func1.getMappedClass().createDelegate(context.classLoader) { _, _ -> true }),
        Func1.wrap(Func1.getMappedClass().createDelegate(context.classLoader) { _, _ ->
            manager(context).toggle(key)
            true
        }),
        null,
        0
    ).instance
}