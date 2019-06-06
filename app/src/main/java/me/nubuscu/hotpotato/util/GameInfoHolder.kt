package me.nubuscu.hotpotato.util

import android.util.Log
import me.nubuscu.hotpotato.model.ClientDetailsModel

/**
 * Singleton to abstract game information
 */
class GameInfoHolder {
    companion object {
        val instance = GameInfoHolder()
    }

    var isHost: Boolean = false
    var endpoints: MutableSet<ClientDetailsModel> = mutableSetOf()
        set(value) {
            field = value
            Log.d("test", "known endpoints in GameInfoHolder: $endpoints")
        }
    var myEndpointId: String? = null

    /**
     * Maintains a map of endpoint IDs to the avatars they use
     * Should include our own endpoint ID and locally stored avatar
     */
    val endpointAvatars: MutableMap<String, ByteArray> = mutableMapOf()
}