package me.nubuscu.hotpotato.model

import me.nubuscu.hotpotato.util.GameInfoHolder

/**
 * Holds a client that has connected to your game
 */
data class ClientDetailsModel (
    val id: String,
    val name: String
) {
    var profilePicture: ByteArray?
        get() = GameInfoHolder.instance.endpointAvatars[this.id]
        set(value) {
            if (value != null) {
                GameInfoHolder.instance.endpointAvatars[this.id] = value
            }
        }

    override fun toString(): String {
        val profilePicDesc = if (profilePicture == null) "null" else "${profilePicture?.size} bytes"
        return "ClientDetailsModel(id='$id', name='$name', profilePicture=$profilePicDesc)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientDetailsModel

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
