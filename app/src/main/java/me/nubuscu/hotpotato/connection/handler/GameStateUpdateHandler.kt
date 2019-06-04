package me.nubuscu.hotpotato.connection.handler

import android.content.Intent
import android.widget.Toast
import me.nubuscu.hotpotato.InGameActivity
import me.nubuscu.hotpotato.model.dto.GameStateUpdateMessage
import me.nubuscu.hotpotato.util.DataHolder

object GameStateUpdateHandler: PayloadHandler<GameStateUpdateMessage>() {
    override fun handle(message: GameStateUpdateMessage) {
        super.handle(message)

        if (message.inProgress) {
            Toast.makeText(DataHolder.instance.context.get(), "Game is starting", Toast.LENGTH_SHORT).show()
            DataHolder.instance.context.get().let {
                val intent = Intent(it, InGameActivity::class.java)
                it?.startActivity(intent)
            }
        } else {
            Toast.makeText(DataHolder.instance.context.get(), "Game has ended", Toast.LENGTH_SHORT).show()
            //TODO return to menu
        }
    }

}