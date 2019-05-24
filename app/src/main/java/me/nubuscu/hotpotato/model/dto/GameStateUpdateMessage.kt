package me.nubuscu.hotpotato.model.dto

/**
 * for notifying devices that the game has started/stopped
 */
data class GameStateUpdateMessage(val inProgress: Boolean): Message("gameState")