package xyz.rodit.snapmod.features.tweaks

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.DiscoverFeedObservableSection
import xyz.rodit.snapmod.mappings.DiscoverViewBinder

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
        DiscoverViewBinder.setSections.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (hiddenStorySections.size > 0) {
                    val sections = param.args[0] as List<*>
                    param.args[0] = sections.filter {
                        !hiddenStorySections.contains(DiscoverFeedObservableSection.wrap(it).model.name)
                    }
                }
            }
        })
    }
}