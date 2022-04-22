package xyz.rodit.snapmod;

import java.util.HashMap;
import java.util.Map;

import xyz.rodit.xposed.client.ConfigurationClient;
import xyz.rodit.xposed.utils.Predicate;

public class TweakHelper {

    private static final Map<String, TweakConfiguration> TWEAK_OVERRIDES = new HashMap<>();

    private static void override(String name, String optionName, Object overrideValue) {
        override(name, c -> c.getBoolean(optionName), overrideValue);
    }

    private static void override(String name, Predicate<ConfigurationClient> optionRequirement, Object overrideValue) {
        TWEAK_OVERRIDES.put(name, new TweakConfiguration(optionRequirement, overrideValue));
    }

    static {
        override("ENABLE_DISCOVER_AD", "block_ads", false);
        override("ENABLE_USER_STORY_AD", "block_ads", false);
        override("ENABLE_ADS_IN_SHOWS", "block_ads", false);
        override("ENABLE_CONTENT_INTERSTITIAL_ADS", "block_ads", false);
        override("ENABLE_COGNAC_AD", "block_ads", false);

        override("SPOTLIGHT_5TH_TAB_ENABLED", "disable_spotlight", false);

        override("PREVENT_STORIES_FROM_BEING_MARKED_AS_VIEWED", "hide_story_views_local", true);

        override("GRAPHENE_HOST", "disable_metrics", "https://127.0.0.1:1");
        override("MAX_RETRY_QUEUE_SIZE", "disable_metrics", 0);
        override("LOG_EVENTS", "disable_metrics", false);
        override("FLUSH_EVENTS_TO_DISK_ON_PAUSE", "disable_metrics", false);
        override("V2_BLIZZARD_DISK_QUOTA", "disable_metrics", 0);
        override("ARE_BENCHMARKS_ENABLED", "disable_metrics", false);
        override("CUSTOM_SPECTRUM_COLLECTOR_URL", "disable_metrics", "https://127.0.0.1:1");
        override("CUSTOM_COLLECTOR_URL", "disable_metrics", "https://127.0.0.1:1");

        override("DF_ADAPTER_REFACTOR_ENABLED", c -> c.getString("disable_story_sections", "[]").length() > 2, true);
    }

    public static Object applyOverride(ConfigurationClient config, String tweakName) {
        TweakConfiguration tweak = TWEAK_OVERRIDES.get(tweakName);
        if (tweak != null && tweak.optionRequirement.test(config)) {
            return tweak.overrideValue;
        }

        return null;
    }

    private static class TweakConfiguration {

        public Predicate<ConfigurationClient> optionRequirement;
        public Object overrideValue;

        public TweakConfiguration(Predicate<ConfigurationClient> optionName, Object overrideValue) {
            this.optionRequirement = optionName;
            this.overrideValue = overrideValue;
        }
    }
}
