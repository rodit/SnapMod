package xyz.rodit.snapmod.features.conversations

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.before

class SnapOverrides(context: FeatureContext) : Feature(context) {

    override fun performHooks() {
        // Hook message sending to convert gallery to live snap.
        MessageSenderCrossroad.apply.before(context, "override_snap") {
            val self = MessageSenderCrossroad.wrap(it.thisObject)
            val container = self.payload.media

            if (!SerializableContent.isInstance(container.instance)) return@before

            val content = SerializableContent.wrap(container.instance)
            val message = content.message
            if (!GallerySnapMedia.isInstance(message.instance)) return@before

            val id = GallerySnapMedia.wrap(message.instance).media.id
            val snap = LiveSnapMedia().apply { mediaId = id }

            val paramPackage = ParameterPackage(
                true,
                0.0,
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

        MessageTypeChecker.isMediaOverLimit.before(context, "override_snap") {
            it.result = false
        }
    }
}