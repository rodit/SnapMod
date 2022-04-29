package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext

abstract class MenuPlugin(context: FeatureContext, val name: String) : Contextual(context) {

    abstract fun shouldCreate(): Boolean

    abstract fun createModel(key: String?): Any

    abstract fun handleEvent(data: String?)
}

fun createEventData(pluginName: String, key: String): String {
    return ChatMenuModifier.EVENT_PREFIX + ChatMenuModifier.EVENT_DELIMITER + pluginName + ChatMenuModifier.EVENT_DELIMITER + key
}