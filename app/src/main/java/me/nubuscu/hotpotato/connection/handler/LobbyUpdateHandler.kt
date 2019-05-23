package me.nubuscu.hotpotato.connection.handler

import android.util.Log
import me.nubuscu.hotpotato.model.dto.LobbyUpdateMessage

class LobbyUpdateHandler: PayloadHandler<LobbyUpdateMessage> {
    override fun handle(message: LobbyUpdateMessage) {
        //use viewmodels etc. to update frontend things
        Log.d("FOO", "recieved lobby update message")
    }

}