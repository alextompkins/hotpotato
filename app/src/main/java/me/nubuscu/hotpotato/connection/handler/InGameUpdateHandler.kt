package me.nubuscu.hotpotato.connection.handler

import me.nubuscu.hotpotato.model.dto.InGameUpdateMessage
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.GameInfoHolder
import me.nubuscu.hotpotato.util.sendToNearbyEndpoint

class InGameUpdateHandler : PayloadHandler<InGameUpdateMessage> {
    override fun handle(message: InGameUpdateMessage) {
        val info = GameInfoHolder.instance
        if (!info.messageIsForMe(message.dest)) {
            if (message.dest in info.endpoints.map { it.id }) {
                sendToNearbyEndpoint(message, message.dest, DataHolder.instance.context.get())
            }
            return
        }
        //The message is for me, process it
        //TODO play the game with the time remaining in the message
    }

}