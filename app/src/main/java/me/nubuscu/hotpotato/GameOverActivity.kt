package me.nubuscu.hotpotato

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.util.GameInfoHolder

class GameOverActivity : ThemedActivity() {
    private lateinit var loser: ClientDetailsModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val backToLobbyButton: Button = findViewById(R.id.backToLobbyButton)
        backToLobbyButton.setOnClickListener {
            finish()
        }

        val gameInfo = GameInfoHolder.instance
        val loserEndpointId = intent.getStringExtra("loserEndpointId")
        loser = gameInfo.endpoints.find { it.id == loserEndpointId }!!

        val loserUsername: TextView = findViewById(R.id.loserUsername)
        loserUsername.text = if (loser.id == gameInfo.myEndpointId) "You" else loser.name

        val roundDuration = intent.getLongExtra("roundDuration", 0)
        val roundLasted: TextView = findViewById(R.id.roundLasted)
        roundLasted.text = resources.getString(R.string.that_round_lasted).format(roundDuration / 1000)
    }

}
