package me.nubuscu.hotpotato.model.dto

import me.nubuscu.hotpotato.model.ClientDetailsModel

/**
 * TODO add metadata as needed
 * @param allPlayers list of client models contain all players (includes host)
 */
data class LobbyUpdateMessage(val allPlayers: MutableList<ClientDetailsModel>) : Message()