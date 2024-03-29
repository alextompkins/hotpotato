package me.nubuscu.hotpotato.model.dto

/**
 * for notifying devices that the game has ended
 */
data class GameEndMessage(
    val loserEndpointId: String,
    val roundDuration: Long
): Message("gameEnd")