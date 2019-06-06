package me.nubuscu.hotpotato.connection.handler

import me.nubuscu.hotpotato.model.dto.GameEndMessage
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.GameInfoHolder
import me.nubuscu.hotpotato.util.sendToNearbyEndpoints

object GameEndHandler : PayloadHandler<GameEndMessage>() {
    override fun handle(message: GameEndMessage) {
        super.handle(message)

        if (GameInfoHolder.instance.isHost) {
            val info = GameInfoHolder.instance

            sendToNearbyEndpoints(message,
                info.endpoints
                    .map { it.id }
                    .filter { it != info.myEndpointId && it != message.loserEndpointId },
                DataHolder.instance.context.get()!!)
        }
    }
}
