package xyz.rodit.snapmod.features.saving

import xyz.rodit.snapmod.features.Feature
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.PathManager
import xyz.rodit.snapmod.util.after
import xyz.rodit.snapmod.util.toMax
import xyz.rodit.snapmod.util.toUUIDString

class AutoDownloadSnaps(context: FeatureContext) : Feature(context, 84608.toMax()) {

    private val ignore = hashSetOf<String>()

    override fun performHooks() {
        OperaPageViewController.onDisplayStateChanged.after {
            val viewController = OperaPageViewController.wrap(it.thisObject)
            if (viewController.state.instance != OperaDisplayState.FULLY_DISPLAYED().instance) return@after

            val params = ParamsMap.wrap(viewController.metadata.instance)
            val map = params.map
            if (!map.containsKey(MessageStoryKeys.getSnapInSavedState().instance)) return@after

            val messageId = map[MessageStoryKeys.getMessageId().instance] as String?
            val conversationId = UUID.wrap(map[ConversationStoryKeys.getConversationId().instance])
            if (messageId == null || conversationId.isNull) return@after

            if (!context.config.getBoolean("auto_download_snaps")
                && !context.autoDownload.isEnabled(conversationId.toUUIDString())
            ) return@after

            if (ignore.contains(messageId)) return@after

            ignore.add(messageId)
            getMediaInfo(context, params) { info ->
                downloadOperaMedia(
                    context,
                    PathManager.DOWNLOAD_SNAP,
                    info
                )
            }
        }
    }
}