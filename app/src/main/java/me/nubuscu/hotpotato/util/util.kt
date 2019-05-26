package me.nubuscu.hotpotato.util

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import me.nubuscu.hotpotato.model.dto.Message
import me.nubuscu.hotpotato.util.serialization.messageGson

/**
 * generic helper to send a Nearby message to a list of endpoints
 */
fun sendToNearbyEndpoints(content: Message, endpoints: List<String>, context: Context) {
    val payload = Payload.fromBytes(messageGson.toJson(content).toByteArray())
    Nearby.getConnectionsClient(context).sendPayload(endpoints, payload)
}