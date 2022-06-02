package xyz.rodit.snapmod.features.saving

import xyz.rodit.snapmod.createDummyProxy
import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.logging.XLog
import xyz.rodit.snapmod.mappings.*
import xyz.rodit.snapmod.util.PathManager
import xyz.rodit.snapmod.util.download
import xyz.rodit.snapmod.util.toSnapUUID
import xyz.rodit.snapmod.util.toUUIDString
import xyz.rodit.xposed.client.http.StreamProvider
import xyz.rodit.xposed.client.http.streams.FileProxyStreamProvider
import java.net.URL

private val mLog = XLog("StoryHelper")

typealias UsernameFetcher = (Map<*, *>) -> String?

private val usernameFetchers = listOf<UsernameFetcher>(
    {
        val session = ContextSession.wrap(it[ContextStoryKeys.getContextSession().instance])
        if (session.isNull) null else {
            val snapUsername = session.info.username
            if (snapUsername.isNotNull) snapUsername.displayString else null
        }
    },
    {
        val storySnap =
            PlayableStorySnap.wrap(it[FriendStoryKeys.getPlayableSnapStoryRecord().instance])
        if (storySnap.isNotNull) storySnap.displayName else null
    }
)

fun downloadOperaMedia(context: FeatureContext, type: String?, media: StoryMedia?) {
    if (media == null || media.info.isNull) return

    val provider: StreamProvider = FileProxyStreamProvider(context.appContext) {
        try {
            var stream = URL(media.info.uri).openStream()
            val enc = media.info.encryption
            if (enc.isNotNull) {
                stream = enc.decryptStream(stream)
            }

            return@FileProxyStreamProvider stream
        } catch (e: Exception) {
            mLog.error("Error opening story media stream.", e)
        }
        return@FileProxyStreamProvider null
    }

    val finalType = type
        ?: if (media.map.containsKey(MessageStoryKeys.getMessageId().instance)) PathManager.DOWNLOAD_SNAP else PathManager.DOWNLOAD_STORY
    val typeString = if (finalType == PathManager.DOWNLOAD_SNAP) "Snap" else "Story"

    context.download(
        finalType,
        mapOf("u" to media.username),
        media.extension,
        provider,
        "${media.username}'s $typeString"
    )
}

fun getMediaInfo(
    context: FeatureContext, metadata: ParamsMap, callback: (StoryMedia?) -> Unit
) {
    val map: Map<*, *> = metadata.map

    getUsername(context, metadata) { username ->
        map[StoryMetadata.getImageMediaInfo().instance]?.let {
            mLog.debug("Found image media info for story.")
            return@getUsername callback(StoryMedia(OperaMediaInfo.wrap(it), username, ".jpg", map))
        }

        map[StoryMetadata.getVideoMediaInfo().instance]?.let {
            mLog.debug("Found video media info for story.")
            return@getUsername callback(StoryMedia(OperaMediaInfo.wrap(it), username, ".mp4", map))
        }

        map[StoryMetadata.getOverlayImageMediaInfo().instance]?.let {
            mLog.debug("Found image overlay media info for story.")
            return@getUsername callback(StoryMedia(OperaMediaInfo.wrap(it), username, ".jpg", map))
        }

        mLog.error("Error getting media info for $metadata.")
        callback(null)
    }
}

private fun getUsername(
    context: FeatureContext, metadata: ParamsMap, callback: (String) -> Unit
) {
    val map: Map<*, *> = metadata.map

    val username = usernameFetchers.firstNotNullOfOrNull { it(map) }
    if (username != null) {
        mLog.debug("Found username using fetchers.")
        return callback(username)
    }

    val messageId = map[MessageStoryKeys.getMessageId().instance]
    mLog.debug("Trying to get username from message id: $messageId")
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
        mLog.debug("Fetched message with username $friendUsername.")
        callback(friendUsername)
        true
    }
    mLog.debug("Fetching message with conversation manager.")
    context.instances.conversationManager.fetchMessage(
        uuid.toSnapUUID(),
        arroyoId,
        messageCallback
    )
}

val ParamsMap.isChat: Boolean
    get() = map.containsKey(MessageStoryKeys.getSnapInSavedState().instance)

val ParamsMap.storyId: String?
    get() {
        val reportingInfo = map[FriendStoryKeys.getStorySnapViewReportingInfo().instance]
        return if (reportingInfo is Collection<*>) StorySnapViewReportingInfo.wrap(reportingInfo.first()).storySnapKey.storyKey.storyId else null
    }

class StoryMedia(
    val info: OperaMediaInfo, val username: String, val extension: String, val map: Map<*, *>
)