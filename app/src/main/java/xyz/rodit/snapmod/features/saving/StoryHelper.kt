package xyz.rodit.snapmod.features.saving

import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.mappings.*

object StoryHelper {

    fun getMediaInfo(metadata: ParamsMap): StoryMedia? {
        val map: Map<*, *> = metadata.map

        var username = "unknown"
        val session = ContextSession.wrap(map[ContextStoryKeys.getContextSession().instance])
        val snapUsername = session.info.username
        if (snapUsername.isNotNull) {
            username = snapUsername.displayString
        } else {
            val storySnap =
                PlayableStorySnap.wrap(map[FriendStoryKeys.getPlayableSnapStoryRecord().instance])
            if (storySnap.isNotNull) {
                username = storySnap.displayName
            }
        }

        map[StoryMetadata.getImageMediaInfo().instance]?.let {
            return StoryMedia(OperaMediaInfo.wrap(it), username, ".jpg")
        }

        map[StoryMetadata.getVideoMediaInfo().instance]?.let {
            return StoryMedia(OperaMediaInfo.wrap(it), username, ".mp4")
        }

        map[StoryMetadata.getOverlayImageMediaInfo().instance]?.let {
            return StoryMedia(OperaMediaInfo.wrap(it), username, ".jpg")
        }

        XposedBridge.log("Error getting media info for $metadata.")
        return null
    }

    class StoryMedia(val info: OperaMediaInfo, val username: String, val extension: String)
}