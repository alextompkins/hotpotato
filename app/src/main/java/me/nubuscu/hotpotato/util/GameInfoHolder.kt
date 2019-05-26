package me.nubuscu.hotpotato.util

import me.nubuscu.hotpotato.model.ClientDetailsModel

/**
 * Singleton to abstract game information
 */
class GameInfoHolder {
    companion object {
        val instance = GameInfoHolder()
    }

    var isHost: Boolean = false
    var endpoints: Set<ClientDetailsModel> = setOf()

    /**
     * the Nearby API doesn't know it's own endpoint ID
     * therefore, assuming we're using P2P_STAR:
     * if I receive a message with a dest I don't know, it must be for me
     */
    fun messageIsForMe(destEndpoint: String) = !endpoints.map { it.id }.contains(destEndpoint)

}