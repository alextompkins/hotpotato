package me.nubuscu.hotpotato.model

/**
 * Holds a client that has connected to your game
 */
data class ClientDetailsModel (
    val id: String,
    val name: String,
    val profilePicture: ByteArray? = null
)
