package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.AdapterBase
import xyz.rodit.snapmod.mappings.DfSectionController
import xyz.rodit.snapmod.util.before
import xyz.rodit.snapmod.util.getList
import xyz.rodit.snapmod.util.toMax

private val legacyMap = mapOf(
    "friends" to "FRIENDS_SECTION",
    "subs" to "SUBSCRIBED_SECTION",
    "for_you" to "FOR_YOU_SECTION"
)

class HideStorySectionsLegacy(context: FeatureContext) : Feature(context, 84612L.toMax()) {

    private val hiddenStorySections: MutableSet<String> = HashSet()

    override fun onConfigLoaded(first: Boolean) {
        hiddenStorySections.clear()
        hiddenStorySections.addAll(
            context.config.getList("disable_story_sections").mapNotNull { legacyMap[it] })
    }

    override fun performHooks() {
        AdapterBase.addSection.before {
            if (!DfSectionController.isInstance(it.args[0])) return@before

            val sectionType = DfSectionController.wrap(it.args[0]).sectionType.instance.toString()
            if (hiddenStorySections.contains(sectionType)) {
                it.result = null
            }
        }
    }
}