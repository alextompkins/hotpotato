package me.nubuscu.hotpotato.util.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.nubuscu.hotpotato.model.dto.*

/**
 * Property that returns a new gson instance each time it is accessed
 * This has all the subtypes registered so that json messages defined in the dto package work correctly
 */
val messageGson: Gson
    get() {
        return GsonBuilder()
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory
                    .of(Message::class.java, "type")
                    .registerSubtype(LobbyUpdateMessage::class.java, "lobbyUpdate")
                    .registerSubtype(GameStateUpdateMessage::class.java, "gameState")
                    .registerSubtype(InGameUpdateMessage::class.java, "inGame")
                    .registerSubtype(YouAreMessage::class.java, "youAre")
            )
            .create()
    }