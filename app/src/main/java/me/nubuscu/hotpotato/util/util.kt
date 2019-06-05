package me.nubuscu.hotpotato.util

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
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
    val info = GameInfoHolder.instance
    val endpoints = info.endpoints.filter { it.id != info.myEndpointId }.map { it.id }
    sendToNearbyEndpoints(content, endpoints, context)
}

fun getRoundDrawable(resources: Resources, contentResolver: ContentResolver, uri: Uri): Drawable {
    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
    val roundDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
    roundDrawable.isCircular = true
    return roundDrawable
}
