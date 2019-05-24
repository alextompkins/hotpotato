package me.nubuscu.hotpotato.connection.handler

import android.widget.Toast
import me.nubuscu.hotpotato.model.dto.GameStateUpdateMessage
import me.nubuscu.hotpotato.util.DataHolder

class GameStateUpdateHandler: PayloadHandler<GameStateUpdateMessage> {
    override fun handle(message: GameStateUpdateMessage) {
        if (message.inProgress) {
            Toast.makeText(DataHolder.instance.context.get(), "Game is starting", Toast.LENGTH_SHORT).show()
            //TODO launch the in-game screen
        } else {
            Toast.makeText(DataHolder.instance.context.get(), "Game has ended", Toast.LENGTH_SHORT).show()
            //TODO return to menu
        }
    }

}