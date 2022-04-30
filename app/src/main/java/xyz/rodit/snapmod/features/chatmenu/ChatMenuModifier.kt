package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.FriendChatActionHandler
import xyz.rodit.snapmod.mappings.FriendChatActionMenuBuilder
import xyz.rodit.snapmod.mappings.RxSingleton
import xyz.rodit.snapmod.mappings.SendChatAction
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.before

const val EVENT_PREFIX = "CUSTOM_ACTION"
const val EVENT_DELIMITER = "\u0000:\u0000"

class ChatMenuModifier(context: FeatureContext) : Feature(context) {

    private val plugins: MutableMap<String, MenuPlugin> = HashMap()

    override fun init() {
        registerPlugin(PinOption(context))
        registerPlugin(StealthOption(context))
    }

    private fun registerPlugin(plugin: MenuPlugin) {
        plugins[plugin.name] = plugin
    }

    override fun performHooks() {
        // Add plugin actions to chat action menu.
        FriendChatActionMenuBuilder.build.after {
            val self = FriendChatActionMenuBuilder.wrap(it.thisObject)
            val key = self.feedInfoHolder.info.key
            val options = RxSingleton.wrap(it.result).value as MutableList<Any>

            options.addAll(
                plugins.values.filter(MenuPlugin::shouldCreate).map { p -> p.createModel(key) })
        }

        // Override plugin action events.
        FriendChatActionHandler.handle.before {
            if (!SendChatAction.isInstance(it.args[0])) return@before

            val action = SendChatAction.wrap(it.args[0])
            val data = action.dataModel.key
            val parts = data.split(EVENT_DELIMITER)
            if (parts[0] == EVENT_PREFIX && parts.size == 3) {
                val pluginName = parts[1]
                plugins[pluginName]?.let { p ->
                    p.handleEvent(parts[2])
                    it.result = null
                }
            }
        }
    }
}