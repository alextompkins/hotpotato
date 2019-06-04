package me.nubuscu.hotpotato.connection.handler

import me.nubuscu.hotpotato.model.dto.InGameUpdateMessage
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.GameInfoHolder
import me.nubuscu.hotpotato.util.sendToNearbyEndpoint

object InGameUpdateHandler : PayloadHandler<InGameUpdateMessage>() {
    override fun handle(message: InGameUpdateMessage) {
        super.handle(message)

        val info = GameInfoHolder.instance
        if (info.myEndpointId != message.dest) {
            if (message.dest in info.endpoints.map { it.id }) {
                sendToNearbyEndpoint(message, message.dest, DataHolder.instance.context.get())
            }
        }
    }

}
