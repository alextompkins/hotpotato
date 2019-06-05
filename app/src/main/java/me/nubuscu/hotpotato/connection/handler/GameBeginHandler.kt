package me.nubuscu.hotpotato.connection.handler

import android.content.Intent
import android.widget.Toast
import me.nubuscu.hotpotato.InGameActivity
import me.nubuscu.hotpotato.model.dto.GameBeginMessage
import me.nubuscu.hotpotato.util.DataHolder

object GameBeginHandler : PayloadHandler<GameBeginMessage>() {
    override fun handle(message: GameBeginMessage) {
        super.handle(message)

        Toast.makeText(DataHolder.instance.context.get(), "Game is starting", Toast.LENGTH_SHORT).show()
        DataHolder.instance.context.get().let {
            val intent = Intent(it, InGameActivity::class.java)
            it?.startActivity(intent)
        }
    }

}