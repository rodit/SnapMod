package xyz.rodit.snapmod.features

import xyz.rodit.snapmod.mappings.ConversationManager
import xyz.rodit.snapmod.util.after

class InstanceManager {

    var conversationManager: ConversationManager = ConversationManager.wrap(null)

    init {
        ConversationManager.constructors.after {
            conversationManager = ConversationManager.wrap(it.thisObject)
        }
    }
}