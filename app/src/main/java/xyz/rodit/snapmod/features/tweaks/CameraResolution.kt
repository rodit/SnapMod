package xyz.rodit.snapmod.features.tweaks

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.MediaQualityLevel
import xyz.rodit.snapmod.mappings.RecordingCodecConfiguration
import xyz.rodit.snapmod.mappings.ScCameraSettings
import xyz.rodit.snapmod.mappings.TranscodingRequest
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.getNonDefault
import xyz.rodit.snapmod.util.getResolution

class CameraResolution(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Override preview and picture resolution
        ScCameraSettings.constructors.after {
            val settings = ScCameraSettings.wrap(it.thisObject)
            context.config.getResolution("custom_video_resolution")?.let { r ->
                val previewResolution = settings.previewResolution
                if (previewResolution.isNotNull) {
                    previewResolution.width = r.width
                    previewResolution.height = r.height
                }
            }

            context.config.getResolution("custom_image_resolution")?.let { r ->
                val pictureResolution = settings.pictureResolution
                if (pictureResolution.isNotNull) {
                    pictureResolution.width = r.width
                    pictureResolution.height = r.height
                }
            }
        }

        // Override actual recording resolution
        RecordingCodecConfiguration.constructors.after {
            val config = RecordingCodecConfiguration.wrap(it.thisObject)
            context.config.getResolution("custom_video_resolution")?.let { r ->
                val res = config.resolution
                res.width = r.width
                res.height = r.height
            }

            context.config.getNonDefault("custom_video_bitrate")?.let { bitrate ->
                config.bitrate = bitrate
            }
        }

        // Override save/send quality level
        TranscodingRequest.constructors.after(context, "force_source_encoding") {
            TranscodingRequest.wrap(it.thisObject).qualityLevel = MediaQualityLevel.LEVEL_MAX()
        }
    }
}