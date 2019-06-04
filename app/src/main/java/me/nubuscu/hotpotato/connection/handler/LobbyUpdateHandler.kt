package me.nubuscu.hotpotato.connection.handler

import android.util.Log
import me.nubuscu.hotpotato.model.dto.LobbyUpdateMessage
import me.nubuscu.hotpotato.util.GameInfoHolder

object LobbyUpdateHandler: PayloadHandler<LobbyUpdateMessage>() {
    override fun handle(message: LobbyUpdateMessage) {
        super.handle(message)

        GameInfoHolder.instance.endpoints.addAll(message.allPlayers)
        //use viewmodels etc. to update frontend things
        Log.d("FOO", "lobby contains: ${message.allPlayers}")
    }
}
