package xyz.rodit.snapmod.features.opera;

import xyz.rodit.snapmod.features.Contextual;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.StoryAutoAdvanceMode;
import xyz.rodit.snapmod.mappings.StoryMediaPlaybackMode;

public class SnapDurationModifier extends Contextual implements OperaPlugin {

    private static final String KEY_AUTO_ADVANCE_MODE = "auto_advance_mode";
    private static final String KEY_MEDIA_PLAYBACK_MODE = "media_playback_mode";

    public SnapDurationModifier(FeatureContext context) {
        super(context);
    }

    @Override
    public boolean isEnabled() {
        return context.config.getBoolean("unlimited_snap_duration");
    }

    @Override
    public boolean shouldOverride(String key) {
        return KEY_AUTO_ADVANCE_MODE.equals(key)
                || KEY_MEDIA_PLAYBACK_MODE.equals(key);
    }

    @Override
    public Object override(String key, Object value) {
        if (KEY_AUTO_ADVANCE_MODE.equals(key)) {
            return StoryAutoAdvanceMode.NO_AUTO_ADVANCE().instance;
        } else if (KEY_MEDIA_PLAYBACK_MODE.equals(key)) {
            return StoryMediaPlaybackMode.LOOPING().instance;
        }

        return null;
    }
}
