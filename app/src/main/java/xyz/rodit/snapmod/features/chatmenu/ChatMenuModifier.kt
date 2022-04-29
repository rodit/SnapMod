package xyz.rodit.snapmod.features.chatmenu

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.FriendChatActionHandler
import xyz.rodit.snapmod.mappings.FriendChatActionMenuBuilder
import xyz.rodit.snapmod.mappings.RxSingleton
import xyz.rodit.snapmod.mappings.SendChatAction

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
        FriendChatActionMenuBuilder.build.hook(object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val self = FriendChatActionMenuBuilder.wrap(param.thisObject)
                val key = self.feedInfoHolder.info.key
                val options = RxSingleton.wrap(param.result).value as MutableList<Any>

                options.addAll(plugins.values.filter { it.shouldCreate() }
                    .map { it.createModel(key) })
            }
        })

        // Override plugin action events.
        FriendChatActionHandler.handle.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!SendChatAction.isInstance(param.args[0])) return

                val action = SendChatAction.wrap(param.args[0])
                val data = action.dataModel.key
                val parts = data.split(EVENT_DELIMITER)
                if (parts[0] == EVENT_PREFIX && parts.size == 3) {
                    val pluginName = parts[1]
                    plugins[pluginName]?.let {
                        it.handleEvent(parts[2])
                        param.result = null
                    }
                }
            }
        })
    }

    companion object {
        const val EVENT_PREFIX = "CUSTOM_ACTION"
        const val EVENT_DELIMITER = "\u0000:\u0000"
    }
}