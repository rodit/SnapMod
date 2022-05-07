package xyz.rodit.snapmod.features.saving

import de.robv.android.xposed.XposedBridge
import xyz.rodit.snapmod.createDummyProxy
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.toSnapUUID
import xyz.rodit.snapmod.util.toUUIDString

typealias UsernameFetcher = (Map<*, *>) -> String?

private val usernameFetchers = listOf<UsernameFetcher>(
    {
        val session = ContextSession.wrap(it[ContextStoryKeys.getContextSession().instance])
        val snapUsername = session.info.username
        if (snapUsername.isNotNull) snapUsername.displayString else null
    },
    {
        val storySnap =
            PlayableStorySnap.wrap(it[FriendStoryKeys.getPlayableSnapStoryRecord().instance])
        if (storySnap.isNotNull) storySnap.displayName else null
    }
)

fun getMediaInfo(
    context: FeatureContext, metadata: ParamsMap, callback: (StoryMedia?) -> Unit
) {
    val map: Map<*, *> = metadata.map

    getUsername(context, metadata) { username ->
        map[StoryMetadata.getImageMediaInfo().instance]?.let {
            return@getUsername callback(StoryMedia(OperaMediaInfo.wrap(it), username, ".jpg"))
        }

        map[StoryMetadata.getVideoMediaInfo().instance]?.let {
            return@getUsername callback(StoryMedia(OperaMediaInfo.wrap(it), username, ".mp4"))
        }

        map[StoryMetadata.getOverlayImageMediaInfo().instance]?.let {
            return@getUsername callback(StoryMedia(OperaMediaInfo.wrap(it), username, ".jpg"))
        }

        XposedBridge.log("Error getting media info for $metadata.")
        callback(null)
    }
}

private fun getUsername(
    context: FeatureContext, metadata: ParamsMap, callback: (String) -> Unit
) {
    val map: Map<*, *> = metadata.map

    val username = usernameFetchers.firstNotNullOfOrNull { it(map) }
    if (username != null) return callback(username)

    val messageId = map[MessageStoryKeys.getMessageId().instance]
    if (messageId !is String) return callback("unknown")

    val idParts = messageId.split(':')
    if (idParts.size != 3) return callback("unknown")

    val uuid = idParts[0]
    val arroyoId = idParts[2].toLong()
    val proxy =
        MessageDummyInterface.wrap(
            MessageDummyInterface.getMappedClass().createDummyProxy(context.classLoader)
        )
    val messageCallback = DefaultFetchMessageCallback(proxy, null, 0, 0)
    context.callbacks.on(
        DefaultFetchMessageCallback::class,
        DefaultFetchMessageCallback.onFetchMessageComplete
    ) {
        val message = Message.wrap(it.args[0])
        val senderId = message.senderId.toUUIDString()
        val friends =
            context.instances.friendsRepository.selectFriendsByUserIds(listOf(senderId))
        val friendUsername =
            if (friends.isNotEmpty()) SelectFriendsByUserIds.wrap(friends[0]).username.displayString else "unknown"
        callback(friendUsername)
        true
    }
    context.instances.conversationManager.fetchMessage(
        uuid.toSnapUUID(),
        arroyoId,
        messageCallback
    )
}

class StoryMedia(val info: OperaMediaInfo, val username: String, val extension: String)