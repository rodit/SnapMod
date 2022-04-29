package xyz.rodit.snapmod.features.chatmenu

import xyz.rodit.snapmod.Shared
import xyz.rodit.snapmod.features.FeatureContext

const val PIN_STRING_NAME = "action_menu_pin_conversation"

class PinOption(context: FeatureContext) : ToggleOption(
    context,
    "pinning",
    context.appContext.resources.getIdentifier(PIN_STRING_NAME, "string", Shared.SNAPCHAT_PACKAGE)
) {

    override fun shouldCreate(): Boolean {
        return context.config.getBoolean("allow_pin_chats")
    }

    override fun handleEvent(key: String?) {
        context.pinned.toggle(key)
    }

    override fun isToggled(key: String?): Boolean {
        return context.pinned.isEnabled(key)
    }
}