package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.MediaPackage
import xyz.rodit.snapmod.mappings.VideoLengthChecker
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.before
import xyz.rodit.snapmod.util.toMax

class BypassVideoLengthGlobal(context: FeatureContext) : Feature(context, 84606.toMax()) {

    private var lastVideoDuration: Long = 0L

    override fun performHooks() {
        VideoLengthChecker.apply.before(context, "bypass_video_length_restrictions") {
            if (!MediaPackage.isInstance(it.args[0])) return@before

            val media = MediaPackage.wrap(it.args[0]).media
            lastVideoDuration = media.videoDurationMs
            media.videoDurationMs = 0L
        }

        VideoLengthChecker.apply.after(context, "bypass_video_length_restrictions") {
            if (!MediaPackage.isInstance(it.args[0])) return@after

            MediaPackage.wrap(it.args[0]).media.videoDurationMs = lastVideoDuration
        }
    }
}