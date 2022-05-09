package xyz.rodit.snapmod.features.messagemenu

import xyz.rodit.dexsearch.client.xposed.MappedObject
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ChatMessageActionBuilder
import xyz.rodit.snapmod.mappings.ChatModelAudioNote
import xyz.rodit.snapmod.mappings.ChatModelBase
import xyz.rodit.snapmod.util.after

class MessageMenuModifier(context: FeatureContext) : Feature(context) {

    private val plugins: MutableList<MenuPlugin> = mutableListOf()

    override fun init() {
        plugins.add(AudioSpeedModifier(context))
    }

    override fun performHooks() {
        ChatMessageActionBuilder.createMenu.after {
            val modelObj = ChatMessageActionBuilder.wrap(it.thisObject).chatModel
            if (!ChatModelAudioNote.isInstance(modelObj)) return@after

            val items = it.result as MutableList<Any>
            val model = ChatModelBase.wrap(modelObj)
            items.addAll(plugins.filter(MenuPlugin::isEnabled)
                .flatMap { p -> p.createOptions(model).map(MappedObject::instance) })
        }

        plugins.forEach(MenuPlugin::performHooks)
    }
}