package me.nubuscu.hotpotato.model.dto


data class InGameUpdateMessage(
    val timeRemaining: Int,
    val dest: String //endpoint ID being sent to
) : Message("inGame")