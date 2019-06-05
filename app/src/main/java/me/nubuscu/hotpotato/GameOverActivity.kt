package me.nubuscu.hotpotato

import android.os.Bundle
import android.widget.Button

class GameOverActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val backToLobbyButton: Button = findViewById(R.id.backToLobbyButton)
        backToLobbyButton.setOnClickListener {
            finish()
        }
    }

}
