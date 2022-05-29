package xyz.rodit.snapmod.features.saving

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
        ContextActionMenuModel.constructors.after(context, "allow_download_stories") {
            val model = ContextActionMenuModel.wrap(it.thisObject)
            if (model.action.instance !== OperaContextAction.SAVE().instance) return@after

            val clickProxy = Func1.getMappedClass().createDelegate(context.classLoader) { _, args ->
                val map = ParamsMap.wrap(args[0])
                getMediaInfo(context, map) { info ->
                    downloadOperaMedia(context, null, info)
                }
                null
            }
            model.onClick = Func1.wrap(clickProxy)
        }
    }
}