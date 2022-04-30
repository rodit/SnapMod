package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.BitmojiUriHandler
import xyz.rodit.snapmod.util.before

class DisableBitmojis(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Disable Bitmoji avatars.
        BitmojiUriHandler.handle.before(context, "disable_bitmojis") { it.result = null }
    }
}