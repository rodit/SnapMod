package xyz.rodit.snapmod.features

import android.app.Activity
import android.content.Context
import xyz.rodit.snapmod.arroyo.ArroyoReader
import xyz.rodit.snapmod.features.callbacks.CallbackManager
import xyz.rodit.snapmod.util.ConversationManager
import xyz.rodit.xposed.client.ConfigurationClient
import xyz.rodit.xposed.client.FileClient
import xyz.rodit.xposed.client.http.StreamServer

private const val PINNED_CONVERSATIONS_FILE = "pinned.list"
private const val STEALTH_CONVERSATIONS_FILE = "stealth.list"
private const val AUTO_SAVE_CONVERSATIONS_FILE = "auto_save.list"
private const val AUTO_DOWNLOAD_CONVERSATIONS_FILE = "auto_download.list"
private const val AUTO_DOWNLOAD_STORIES_FILE = "auto_download_stories.list"
private const val PINNED_STORIES_FILE = "pinned_stories.list"

class FeatureContext(
    val appContext: Context,
    val classLoader: ClassLoader,
    val config: ConfigurationClient,
    val files: FileClient,
    val server: StreamServer,
    val instances: InstanceManager,
    val appVersion: Long
) {
    val features: FeatureManager = FeatureManager(this)
    val callbacks: CallbackManager = CallbackManager()
    val pinned: ConversationManager = ConversationManager(appContext.filesDir, PINNED_CONVERSATIONS_FILE)
    val stealth: ConversationManager = ConversationManager(appContext.filesDir, STEALTH_CONVERSATIONS_FILE)
    val autoSave: ConversationManager = ConversationManager(appContext.filesDir, AUTO_SAVE_CONVERSATIONS_FILE)
    val autoDownload: ConversationManager = ConversationManager(appContext.filesDir, AUTO_DOWNLOAD_CONVERSATIONS_FILE)
    val autoDownloadStories: ConversationManager = ConversationManager(appContext.filesDir, AUTO_DOWNLOAD_STORIES_FILE)
    val pinnedStories: ConversationManager = ConversationManager(appContext.filesDir, PINNED_STORIES_FILE)
    val arroyo = ArroyoReader(appContext)
    val views = ViewInterceptor(this)

    var activity: Activity? = null
}