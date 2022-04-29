package xyz.rodit.snapmod.features.conversations

import xyz.rodit.snapmod.features.FeatureContext
import xyz.rodit.snapmod.mappings.ConversationManager
import xyz.rodit.snapmod.util.UUIDUtil

class PreventReadReceipts(context: FeatureContext) : StealthFeature(context) {

    override fun init() {
        setClass(ConversationManager.getMapping())

        putFilters(
            ConversationManager.displayedMessages,
            { null },
            { UUIDUtil.fromSnap(it.args[0]) },
            NullFilter(context, "hide_read")
        )
    }
}