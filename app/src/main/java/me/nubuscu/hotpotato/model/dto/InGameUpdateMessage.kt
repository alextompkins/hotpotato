package me.nubuscu.hotpotato.model.dto


data class InGameUpdateMessage(
    val timeRemaining: Int,
    val source: String, //this player's username
    val dest: String //username of the player being passed to
) : Message("inGame")