package xyz.rodit.snapmod;

import java.util.HashMap;
import java.util.Map;

import xyz.rodit.xposed.client.ConfigurationClient;

public class TweakHelper {

    private static final Map<String, TweakConfiguration> TWEAK_OVERRIDES = new HashMap<>();

    private static void override(String name, String optionName, Object overrideValue) {
        TWEAK_OVERRIDES.put(name, new TweakConfiguration(optionName, overrideValue));
    }

    static {
        override("ENABLE_DISCOVER_AD", "block_ads", false);
        override("ENABLE_USER_STORY_AD", "block_ads", false);
        override("ENABLE_ADS_IN_SHOWS", "block_ads", false);
        override("ENABLE_CONTENT_INTERSTITIAL_ADS", "block_ads", false);
        override("ENABLE_COGNAC_AD", "block_ads", false);

        override("SPOTLIGHT_5TH_TAB_ENABLED", "disable_spotlight", false);

        override("PREVENT_STORIES_FROM_BEING_MARKED_AS_VIEWED", "hide_story_views", true);

        override("GRAPHENE_HOST", "disable_metrics", "https://127.0.0.1:1");
        override("MAX_RETRY_QUEUE_SIZE", "disable_metrics", 0);
        override("LOG_EVENTS", "disable_metrics", false);
        override("FLUSH_EVENTS_TO_DISK_ON_PAUSE", "disable_metrics", false);
        override("V2_BLIZZARD_DISK_QUOTA", "disable_metrics", 0);
        override("ARE_BENCHMARKS_ENABLED", "disable_metrics", false);
        override("CUSTOM_SPECTRUM_COLLECTOR_URL", "disable_metrics", "https://127.0.0.1:1");
        override("CUSTOM_COLLECTOR_URL", "disable_metrics", "https://127.0.0.1:1");
    }

    public static Object applyOverride(ConfigurationClient config, String tweakName) {
        TweakConfiguration tweak = TWEAK_OVERRIDES.get(tweakName);
        if (tweak != null && config.getBoolean(tweak.optionName)) {
            return tweak.overrideValue;
        }

        return null;
    }

    private static class TweakConfiguration {

        public String optionName;
        public Object overrideValue;

        public TweakConfiguration(String optionName, Object overrideValue) {
            this.optionName = optionName;
            this.overrideValue = overrideValue;
        }
    }
}
