package me.nubuscu.hotpotato.util

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import me.nubuscu.hotpotato.model.dto.Message
import me.nubuscu.hotpotato.util.serialization.messageGson

/**
 * generic helper to send a Nearby message to a list of endpoints
 */
fun sendToNearbyEndpoints(content: Message, endpoints: List<String>, context: Context?) {
    context?.let {
        val payload = Payload.fromBytes(messageGson.toJson(content).toByteArray())
        Nearby.getConnectionsClient(it).sendPayload(endpoints, payload)
    }
}

fun sendToNearbyEndpoint(content: Message, endpoint: String, context: Context?) =
    sendToNearbyEndpoints(content, listOf(endpoint), context)

fun sendToAllNearbyEndpoints(content: Message, context: Context?) {
    val endpoints = GameInfoHolder.instance.endpoints.map { it.id }
    sendToNearbyEndpoints(content, endpoints, context)
}