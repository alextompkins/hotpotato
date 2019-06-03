package me.nubuscu.hotpotato.model.dto

/**
 * The Nearby API is not aware of it's own endpoint ID. these messages are to be send on connection to tell the client
 * what their endpoint is
 */
data class YouAreMessage(val endpoint: String) : Message("youAre")