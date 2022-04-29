package xyz.rodit.snapmod.features.opera

import xyz.rodit.snapmod.features.Contextual
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.StoryAutoAdvanceMode
import xyz.rodit.snapmod.mappings.StoryMediaPlaybackMode

private const val KEY_AUTO_ADVANCE_MODE = "auto_advance_mode"
private const val KEY_MEDIA_PLAYBACK_MODE = "media_playback_mode"

class SnapDurationModifier(context: FeatureContext) : Contextual(context), OperaPlugin {

    override val isEnabled: Boolean
        get() = context.config.getBoolean("unlimited_snap_duration")

    override fun shouldOverride(key: String): Boolean {
        return KEY_AUTO_ADVANCE_MODE == key || KEY_MEDIA_PLAYBACK_MODE == key
    }

    override fun override(key: String, value: Any): Any {
        if (KEY_AUTO_ADVANCE_MODE == key) {
            return StoryAutoAdvanceMode.NO_AUTO_ADVANCE().instance
        } else if (KEY_MEDIA_PLAYBACK_MODE == key) {
            return StoryMediaPlaybackMode.LOOPING().instance
        }

        return value
    }
}