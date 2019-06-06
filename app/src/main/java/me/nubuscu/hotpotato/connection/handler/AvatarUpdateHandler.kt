package me.nubuscu.hotpotato.connection.handler

import me.nubuscu.hotpotato.model.dto.AvatarUpdateMessage
import me.nubuscu.hotpotato.util.GameInfoHolder

object AvatarUpdateHandler : PayloadHandler<AvatarUpdateMessage>() {
    override fun handle(message: AvatarUpdateMessage) {
        super.handle(message)

        // Set the updated avatar in our local storage
        val client = GameInfoHolder.instance.endpoints.find { it.id == message.endpointId }
        if (client != null) {
            client.profilePicture = message.profilePicture
        }
    }
}
