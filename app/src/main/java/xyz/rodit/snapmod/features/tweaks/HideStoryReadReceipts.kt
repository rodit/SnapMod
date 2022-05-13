package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.UploadSnapReadReceiptDurableJobProcessor
import xyz.rodit.snapmod.util.before

class HideStoryReadReceipts(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Prevent story read receipt uploads.
        UploadSnapReadReceiptDurableJobProcessor.uploadReadReceipts.before(
            context, "hide_story_views"
        ) { it.result = null }
    }
}
