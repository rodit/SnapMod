package xyz.rodit.snapmod.features

import xyz.rodit.snapmod.features.chatmenu.ChatMenuModifier
import xyz.rodit.snapmod.features.chatmenu.new.NewChatMenuModifier
import xyz.rodit.snapmod.features.conversations.*
import xyz.rodit.snapmod.features.friendsfeed.FeedModifier
import xyz.rodit.snapmod.features.info.AdditionalFriendInfo
import xyz.rodit.snapmod.features.info.NetworkLogging
import xyz.rodit.snapmod.features.messagemenu.MessageMenuModifier
import xyz.rodit.snapmod.features.notifications.FilterTypes
import xyz.rodit.snapmod.features.notifications.ShowMessageContent
import xyz.rodit.snapmod.features.opera.OperaModelModifier
import xyz.rodit.snapmod.features.saving.*
import xyz.rodit.snapmod.features.tweaks.*

class FeatureManager(context: FeatureContext) : Contextual(context) {

    private val features: MutableList<Feature> = ArrayList()

    fun load() {
        // Chat context menu
        add(::ChatMenuModifier)
        add(::NewChatMenuModifier)

        // Friends feed
        add(::FeedModifier)

        // Conversations/chats
        add(::AutoSave)
        add(::MessageInterceptor)
        add(::PreventBitmojiPresence)
        add(::PreventReadReceipts)
        add(::PreventTypingNotifications)
        add(::SnapInteractionFilter)
        add(::SnapOverrides)

        // Message context menu
        add(::MessageMenuModifier)

        // Notifications
        add(::FilterTypes)
        add(::ShowMessageContent)

        // Opera (story/snap view)
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
        add(::ConfigurationTweaks)
        add(::ConfigurationTweaks)
        add(::DisableBitmojis)
        // add(::FriendAddOverride);
        add(::HideFriends)
        add(::HideStoryReadReceipts)
        add(::HideStorySections)
        add(::LocationOverride)
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
}