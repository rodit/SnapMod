package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.CompositeConfigurationProvider
import xyz.rodit.snapmod.mappings.ConfigKeyBase
import xyz.rodit.snapmod.util.after
import xyz.rodit.xposed.client.ConfigurationClient
import xyz.rodit.xposed.utils.Predicate

private const val DUMMY_URL = "https://127.0.0.1:1"

class ConfigurationTweaks(context: FeatureContext) : Feature(context) {

    private val tweaks: MutableMap<String, Tweak> = HashMap()

    private fun override(name: String, optionName: String, overrideValue: Any) {
        override(name, { it.getBoolean(optionName) }, overrideValue)
    }

    private fun override(
        name: String,
        optionRequirement: Predicate<ConfigurationClient>,
        overrideValue: Any
    ) {
        tweaks[name] = Tweak(optionRequirement, overrideValue)
    }

    override fun init() {
        override("ENABLE_DISCOVER_AD", "block_ads", false)
        override("ENABLE_USER_STORY_AD", "block_ads", false)
        override("ENABLE_ADS_IN_SHOWS", "block_ads", false)
        override("ENABLE_CONTENT_INTERSTITIAL_ADS", "block_ads", false)
        override("ENABLE_COGNAC_AD", "block_ads", false)

        override("SPOTLIGHT_5TH_TAB_ENABLED", "disable_spotlight", false)

        override("PREVENT_STORIES_FROM_BEING_MARKED_AS_VIEWED", "hide_story_views_local", true)

        override("GRAPHENE_HOST", "disable_metrics", DUMMY_URL)
        override("MAX_RETRY_QUEUE_SIZE", "disable_metrics", 0)
        override("LOG_EVENTS", "disable_metrics", false)
        override("FLUSH_EVENTS_TO_DISK_ON_PAUSE", "disable_metrics", false)
        override("V2_BLIZZARD_DISK_QUOTA", "disable_metrics", 0)
        override("ARE_BENCHMARKS_ENABLED", "disable_metrics", false)
        override("CUSTOM_SPECTRUM_COLLECTOR_URL", "disable_metrics", DUMMY_URL)
        override("CUSTOM_COLLECTOR_URL", "disable_metrics", DUMMY_URL)

        override(
            "DF_ADAPTER_REFACTOR_ENABLED",
            { it.getString("disable_story_sections", "[]").length > 2 },
            true
        )

        override("ENABLE_DF_FRIENDS_SECTION_LIST_VIEW", "enable_story_list", true)

        override("ENABLE_VOICE_NOTE_REVAMP", "enable_new_voice_notes", true)
        override("ENABLE_VOICE_NOTES_MESSAGING_PLUGIN", "enable_new_voice_notes", true)

        override("ENABLE_LONG_SNAPS", "disable_snap_splitting", true)
    }

    override fun performHooks() {
        // Apply tweak overrides based on configuration.
        CompositeConfigurationProvider.get.after {
            val key = ConfigKeyBase.wrap(it.args[0])
            val tweak = tweaks[key.name]
            if (tweak != null && tweak.optionRequirement.test(context.config)) {
                it.result = tweak.overrideValue
            }
        }
    }

    private class Tweak(
        val optionRequirement: Predicate<ConfigurationClient>,
        val overrideValue: Any
    )
}