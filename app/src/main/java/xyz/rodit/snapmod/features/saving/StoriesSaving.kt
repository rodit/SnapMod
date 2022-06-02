package xyz.rodit.snapmod.features.saving

import xyz.rodit.snapmod.CustomResources
import xyz.rodit.snapmod.createDelegate
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ContextActionMenuModel
import xyz.rodit.snapmod.mappings.Func1
import xyz.rodit.snapmod.mappings.OperaContextAction
import xyz.rodit.snapmod.mappings.ParamsMap
import xyz.rodit.snapmod.util.after

class StoriesSaving(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Override save story click.
        // Override custom options click.
        ContextActionMenuModel.constructors.after(context, "allow_download_stories") {
            val model = ContextActionMenuModel.wrap(it.thisObject)
            if (model.action.instance == OperaContextAction.SAVE().instance) {
                val clickProxy =
                    Func1.getMappedClass().createDelegate(context.classLoader) { _, args ->
                        val params = ParamsMap.wrap(args[0])
                        getMediaInfo(context, params) { info ->
                            downloadOperaMedia(context, null, info)
                        }
                        null
                    }
                model.onClick = Func1.wrap(clickProxy)
            } else if (model.text == CustomResources.get(CustomResources.string.menu_story_disable_auto_download)) {
                model.onClick = createAutoDownloadClickProxy(false)
            } else if (model.text == CustomResources.get(CustomResources.string.menu_story_enable_auto_download)) {
                model.onClick = createAutoDownloadClickProxy(true)
            }
        }
    }

    private fun createAutoDownloadClickProxy(enable: Boolean) =
        Func1.wrap(
            Func1.getMappedClass().createDelegate(context.classLoader) { _, args ->
                val params = ParamsMap.wrap(args[0])
                val storyId = params.storyId
                if (enable) context.autoDownloadStories.enable(storyId)
                else context.autoDownloadStories.disable(storyId)
                null
            }
        )
}