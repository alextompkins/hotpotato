package me.nubuscu.hotpotato.connection.handler

import android.widget.Toast
import me.nubuscu.hotpotato.model.dto.GameEndMessage
import me.nubuscu.hotpotato.util.DataHolder

object GameEndHandler : PayloadHandler<GameEndMessage>() {
    override fun handle(message: GameEndMessage) {
        super.handle(message)
        Toast.makeText(DataHolder.instance.context.get(), "Game has ended", Toast.LENGTH_SHORT).show()
    }
}
