package me.nubuscu.hotpotato.util

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
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
        val json = messageGson.toJson(content)
        Log.i("SENDING_PAYLOAD", "size: ${json.length}, data: $json")
        val payload = Payload.fromBytes(json.toByteArray())
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
    return makeRoundDrawableFromBitmap(resources, bitmap)
}

fun makeRoundDrawableFromBitmap(resources: Resources, bitmap: Bitmap): Drawable {
    val roundDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
    roundDrawable.isCircular = true
    return roundDrawable
}

fun makeBitmap(data: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(data, 0, data.size)
}
