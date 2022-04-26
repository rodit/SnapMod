package xyz.rodit.snapmod.features.tweaks;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.CompositeConfigurationProvider;
import xyz.rodit.snapmod.mappings.ConfigKeyBase;
import xyz.rodit.xposed.client.ConfigurationClient;
import xyz.rodit.xposed.utils.Predicate;

public class ConfigurationTweaks extends Feature {

    private static final String DUMMY_URL = "https://127.0.0.1:1";

    private final Map<String, Tweak> tweaks = new HashMap<>();

    public ConfigurationTweaks(FeatureContext context) {
        super(context);
    }

    private void override(String name, String optionName, Object overrideValue) {
        override(name, c -> c.getBoolean(optionName), overrideValue);
    }

    private void override(String name, Predicate<ConfigurationClient> optionRequirement, Object overrideValue) {
        tweaks.put(name, new Tweak(optionRequirement, overrideValue));
    }

    @Override
    protected void init() {
        override("ENABLE_DISCOVER_AD", "block_ads", false);
        override("ENABLE_USER_STORY_AD", "block_ads", false);
        override("ENABLE_ADS_IN_SHOWS", "block_ads", false);
        override("ENABLE_CONTENT_INTERSTITIAL_ADS", "block_ads", false);
        override("ENABLE_COGNAC_AD", "block_ads", false);

        override("SPOTLIGHT_5TH_TAB_ENABLED", "disable_spotlight", false);

        override("PREVENT_STORIES_FROM_BEING_MARKED_AS_VIEWED", "hide_story_views_local", true);

        override("GRAPHENE_HOST", "disable_metrics", DUMMY_URL);
        override("MAX_RETRY_QUEUE_SIZE", "disable_metrics", 0);
        override("LOG_EVENTS", "disable_metrics", false);
        override("FLUSH_EVENTS_TO_DISK_ON_PAUSE", "disable_metrics", false);
        override("V2_BLIZZARD_DISK_QUOTA", "disable_metrics", 0);
        override("ARE_BENCHMARKS_ENABLED", "disable_metrics", false);
        override("CUSTOM_SPECTRUM_COLLECTOR_URL", "disable_metrics", DUMMY_URL);
        override("CUSTOM_COLLECTOR_URL", "disable_metrics", DUMMY_URL);

        override("DF_ADAPTER_REFACTOR_ENABLED", c -> c.getString("disable_story_sections", "[]").length() > 2, true);
    }

    @Override
    protected void performHooks() {
        // Apply tweak overrides based on configuration.
        CompositeConfigurationProvider.get.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                ConfigKeyBase key = ConfigKeyBase.wrap(param.args[0]);
                Tweak tweak = tweaks.get(key.getName());
                if (tweak != null && tweak.optionRequirement.test(context.config)) {
                    param.setResult(tweak.overrideValue);
                }
            }
        });
    }

    private static class Tweak {

        public Predicate<ConfigurationClient> optionRequirement;
        public Object overrideValue;

        public Tweak(Predicate<ConfigurationClient> optionName, Object overrideValue) {
            this.optionRequirement = optionName;
            this.overrideValue = overrideValue;
        }
    }
}
