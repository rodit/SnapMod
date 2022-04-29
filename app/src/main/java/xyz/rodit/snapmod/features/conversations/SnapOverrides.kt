package xyz.rodit.snapmod.features.conversations

import de.robv.android.xposed.XC_MethodHook
import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*

class SnapOverrides(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Hook message sending to convert gallery to live snap.
        MessageSenderCrossroad.apply.hook(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!context.config.getBoolean("override_snap")) return

                val self = MessageSenderCrossroad.wrap(param.thisObject)
                val container = self.payload.media

                if (!SerializableContent.isInstance(container.instance)) return

                val content = SerializableContent.wrap(container.instance)
                val message = content.message
                if (GallerySnapMedia.isInstance(message.instance)) {
                    val id = GallerySnapMedia.wrap(message.instance).media.id
                    val snap = LiveSnapMedia()
                    val timer = context.config.getString("override_snap_timer", "0").toDouble()
                    snap.mediaId = id
                    if (context.config.getBoolean("enable_snap_type_override")) {
                        val overrideType = context.config.getString("snap_type_override", "IMAGE")
                        snap.mediaType = MediaType.valueOf(overrideType)
                    } else {
                        snap.mediaType = MediaType.IMAGE()
                    }

                    val paramPackage = ParameterPackage(
                        timer == 0.0,
                        timer,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false
                    )
                    snap.parameterPackage = paramPackage
                    content.message = MediaBaseBase.wrap(snap.instance)
                }
            }
        })
    }
}