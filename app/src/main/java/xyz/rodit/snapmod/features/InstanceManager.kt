package xyz.rodit.snapmod.features

import xyz.rodit.snapmod.mappings.ArroyoChatCommandsClient
import xyz.rodit.snapmod.mappings.ConversationManager
import xyz.rodit.snapmod.mappings.FriendsRepository
import xyz.rodit.snapmod.util.after

class InstanceManager {

    var conversationManager: ConversationManager = ConversationManager.wrap(null)
    var friendsRepository: FriendsRepository = FriendsRepository.wrap(null)
    var chatCommandsClient: ArroyoChatCommandsClient = ArroyoChatCommandsClient.wrap(null)

    init {
        ConversationManager.constructors.after {
            conversationManager = ConversationManager.wrap(it.thisObject)
        }

        FriendsRepository.constructors.after {
            friendsRepository = FriendsRepository.wrap(it.thisObject)
        }

        ArroyoChatCommandsClient.constructors.after {
            chatCommandsClient = ArroyoChatCommandsClient.wrap(it.thisObject)
        }
    }
}