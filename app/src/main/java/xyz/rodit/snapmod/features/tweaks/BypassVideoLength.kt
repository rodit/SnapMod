package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.MemoriesPickerVideoDurationConfig
import xyz.rodit.snapmod.util.before

class BypassVideoLength(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Bypass video duration limit from gallery in chat.
        MemoriesPickerVideoDurationConfig.constructors.before(
            context, "bypass_video_length_restrictions"
        ) { it.args[0] = Long.MAX_VALUE }
    }
}