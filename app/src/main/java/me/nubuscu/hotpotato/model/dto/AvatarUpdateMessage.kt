package me.nubuscu.hotpotato.model.dto

data class AvatarUpdateMessage(
    val endpointId: String,
    val profilePicture: ByteArray? = null
) : Message("avatarUpdate")