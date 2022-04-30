package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.DiscoverFeedObservableSection
import xyz.rodit.snapmod.mappings.DiscoverViewBinder
import xyz.rodit.snapmod.util.before

class HideStorySections(context: FeatureContext) : Feature(context) {

    private val hiddenStorySections: MutableSet<String> = HashSet()

    override fun onConfigLoaded(first: Boolean) {
        hiddenStorySections.clear()

        hiddenStorySections.addAll(context.config.getString("disable_story_sections", "[]")
            .drop(1).dropLast(1)
            .split(',')
            .filter { it.isNotBlank() }
            .map { it.trim() })
    }

    override fun performHooks() {
        // Hide story sections.
        DiscoverViewBinder.setSections.before {
            if (hiddenStorySections.size > 0) {
                val sections = it.args[0] as List<*>
                it.args[0] = sections.filter { section ->
                    !hiddenStorySections.contains(
                        DiscoverFeedObservableSection.wrap(section).model.name
                    )
                }
            }
        }
    }
}