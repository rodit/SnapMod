package xyz.rodit.snapmod.features

import okhttp3.internal.toImmutableList
import xyz.rodit.snapmod.features.chatmenu.ChatMenuModifier
import xyz.rodit.snapmod.features.chatmenu.new.NewChatMenuModifier
import xyz.rodit.snapmod.features.conversations.*
import xyz.rodit.snapmod.features.friendsfeed.PinChats
import xyz.rodit.snapmod.features.info.AdditionalFriendInfo
import xyz.rodit.snapmod.features.info.NetworkLogging
import xyz.rodit.snapmod.features.notifications.FilterTypes
import xyz.rodit.snapmod.features.notifications.ShowMessageContent
import xyz.rodit.snapmod.features.opera.CustomStoryOptions
import xyz.rodit.snapmod.features.opera.OperaModelModifier
import xyz.rodit.snapmod.features.saving.*
import xyz.rodit.snapmod.features.tweaks.*

class FeatureManager(context: FeatureContext) : Contextual(context) {

    private val features: MutableList<Feature> = ArrayList()
    val pubFeatures get() = features.toImmutableList()

    fun load() {
        // Chat context menu
        add(::ChatMenuModifier)
        add(::NewChatMenuModifier)

        // Friends feed
        add(::PinChats)

        // Conversations/chats
        add(::AutoSave)
        add(::MessageInterceptor)
        add(::PreventBitmojiPresence)
        add(::PreventReadReceipts)
        add(::PreventTypingNotifications)
        add(::SnapInteractionFilter)
        add(::SnapOverrides)

        // Notifications
        add(::FilterTypes)
        add(::ShowMessageContent)

        // Opera (story/snap view)
        add(::CustomStoryOptions)
        add(::OperaModelModifier)

        // Saving
        add(::AutoDownloadSnaps)
        add(::AutoDownloadStories)
        add(::ChatSaving)
        add(::PublicProfileSaving)
        add(::StoriesSaving)

        // Information
        add(::AdditionalFriendInfo)
        add(::NetworkLogging)

        // Tweaks
        add(::BypassVideoLength)
        add(::BypassVideoLengthGlobal)
        add(::CameraResolution)
        add(::ConfigurationTweaks)
        add(::ConfigurationTweaks)
        add(::DisableBitmojis)
        // add(::HideFriends)
        add(::HideStoryReadReceipts)
        add(::HideStorySections)
        add(::HideStorySectionsLegacy)
        add(::PinStories)
    }

    fun init() {
        features.forEach { it.init() }
    }

    fun onConfigLoaded(first: Boolean) {
        features.forEach { it.onConfigLoaded(first) }
    }

    fun performHooks() {
        features.forEach { it.performHooks() }
    }

    fun add(supplier: (FeatureContext) -> Feature) {
        val feature = supplier(context)
        if (feature.support.contains(context.appVersion)) {
            features.add(feature)
        }
    }

    inline fun <reified T> get(): T where T : Feature {
        return pubFeatures.filterIsInstance<T>().first()
    }
}