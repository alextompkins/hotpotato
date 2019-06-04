package me.nubuscu.hotpotato.model.dto


data class InGameUpdateMessage(
    val timeRemaining: Long,
    val dest: String //endpoint ID being sent to
) : Message("inGame")