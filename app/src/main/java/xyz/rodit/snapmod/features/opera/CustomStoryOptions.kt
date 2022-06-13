package xyz.rodit.snapmod.features.opera

import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.setPadding
import xyz.rodit.snapmod.CustomResources
import xyz.rodit.snapmod.CustomResources.string.menu_story_custom_options
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.features.saving.isChat
import xyz.rodit.snapmod.features.saving.storyId
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.ConversationManager
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.dp
import java.lang.ref.WeakReference

class CustomStoryOptions(context: FeatureContext) : Feature(context), MenuPlugin {

    private var currentParams: WeakReference<Any>? = null

    override val isEnabled = true

    override fun createActions(params: ParamsMap): Collection<OperaActionMenuOptionViewModel> {
        return if (params.storyId == null) emptySet() else setOf(
            OperaActionMenuOptionViewModel(
                0,
                menu_story_custom_options,
                true,
                OperaContextAction.HIDE_AD()
            )
        )
    }

    override fun performHooks() {
        OperaPageViewController.onDisplayStateChanged.after {
            val viewController = OperaPageViewController.wrap(it.thisObject)
            if (viewController.state.instance != OperaDisplayState.FULLY_DISPLAYED().instance) return@after

            val params = ParamsMap.wrap(viewController.metadata.instance)
            if (params.isChat) return@after

            currentParams = WeakReference(params.instance)
        }

        context.views.onAdd("context_action_view") { parent, view ->
            if (view !is ViewGroup) return@onAdd
            if (view.children.filterIsInstance<TextView>()
                    .first().text != CustomResources.get(menu_story_custom_options)
            ) return@onAdd

            parent.removeView(view)
            parent.addView(createToggle("Auto-Download", context.autoDownloadStories))
            parent.addView(createToggle("Pin", context.pinnedStories))
        }
    }

    private fun createToggle(title: String, manager: ConversationManager): Switch {
        val storyId = ParamsMap.wrap(currentParams!!.get()).storyId
        return Switch(context.appContext).apply {
            text = title
            isChecked = manager.isEnabled(storyId)
            setPadding(16.dp)
            setOnCheckedChangeListener { _, toggled ->
                if (toggled) manager.enable(storyId)
                else manager.disable(storyId)
            }
        }
    }
}