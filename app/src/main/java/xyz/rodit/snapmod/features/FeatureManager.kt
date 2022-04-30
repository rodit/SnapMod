package xyz.rodit.snapmod.features

import xyz.rodit.snapmod.features.chatmenu.ChatMenuModifier
import xyz.rodit.snapmod.features.conversations.*
import xyz.rodit.snapmod.features.friendsfeed.FeedModifier
import xyz.rodit.snapmod.features.info.AdditionalFriendInfo
import xyz.rodit.snapmod.features.info.NetworkLogging
import xyz.rodit.snapmod.features.opera.OperaModelModifier
import xyz.rodit.snapmod.features.saving.ChatSaving
import xyz.rodit.snapmod.features.saving.PublicProfileSaving
import xyz.rodit.snapmod.features.saving.StoriesSaving
import xyz.rodit.snapmod.features.tweaks.*
import java.util.function.Function

class FeatureManager(context: FeatureContext) : Contextual(context) {

    private val features: MutableList<Feature> = ArrayList()

    fun load() {
        // Chat context menu
        add(::ChatMenuModifier)

        // Friends feed
        add(::FeedModifier)

        // Conversations/chats
        add(::MessageInterceptor)
        add(::PreventBitmojiPresence)
        add(::PreventReadReceipts)
        add(::PreventTypingNotifications)
        add(::SnapInteractionFilter)
        add(::SnapOverrides)

        // Opera (story/snap view)
        add(::OperaModelModifier)

        // Saving
        add(::ChatSaving)
        add(::PublicProfileSaving)
        add(::StoriesSaving)

        // Information
        add(::AdditionalFriendInfo)
        add(::NetworkLogging)

        // Tweaks
        add(::BypassVideoLength)
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

    fun add(supplier: Function<FeatureContext, Feature>) {
        features.add(supplier.apply(context))
    }
}