package me.nubuscu.hotpotato.connection

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import me.nubuscu.hotpotato.connection.handler.*
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.model.dto.*
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.GameInfoHolder
import me.nubuscu.hotpotato.util.sendToNearbyEndpoint
import me.nubuscu.hotpotato.util.serialization.messageGson

/**
 * Callback used to handle lifecycle events. Called when advertising to host a game
 */
class ConnectionLifecycleCallback(private val viewModel: AvailableConnectionsViewModel) :
    ConnectionLifecycleCallback() {
    private val tentativeConnections = mutableSetOf<ClientDetailsModel>()

    override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
        tentativeConnections.add(ClientDetailsModel(endpointId, connectionInfo.endpointName))
        Nearby.getConnectionsClient(DataHolder.instance.context.get()!!).acceptConnection(endpointId, MessageHandler)
    }

    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        val client = tentativeConnections.find { it.id == endpointId }
        client?.let { tentativeConnections.removeAll { it.id == endpointId } }
        when (result.status.statusCode) {
            ConnectionsStatusCodes.STATUS_OK -> {
                Log.d("FOO", "hey, that's pretty good")
                val list = viewModel.connected.value ?: mutableListOf()
                list.add(client ?: ClientDetailsModel(endpointId, "Unknown"))
                GameInfoHolder.instance.endpoints.addAll(list)
                viewModel.connected.postValue(list)

                val context = DataHolder.instance.context.get()!!
                // Let the connected endpoint their own endpointId
                sendToNearbyEndpoint(YouAreMessage(endpointId), endpointId, context)

                // Send them all the avatars we know about AFTER we know our own endpointId
                Handler(Looper.getMainLooper()).postDelayed({
                    sendKnownAvatars(context, endpointId)
                }, 1000)
            }
            ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Log.d("FOO", "oh no")
            ConnectionsStatusCodes.STATUS_ERROR -> Log.d("FOO", "that's not good")
            else -> {
                Log.d("FOO", "unknown connection code: ${result.status.statusCode}")
            }
        }
    }

    /**
     * Send all known avatars to the newly-connected endpoint (excluding its own)
     */
    private fun sendKnownAvatars(context: Context, endpointId: String) {
        GameInfoHolder.instance.endpointAvatars
            .filter { it.key != endpointId }
            .forEach { sendToNearbyEndpoint(AvatarUpdateMessage(it.key, it.value), endpointId, context) }
    }

    override fun onDisconnected(endpointId: String) {
        Log.i("network", "disconnected from endpoint with id: $endpointId")
        // because removeIf requires a higher api version
        val list = viewModel.connected.value ?: mutableListOf()
        list.removeAll { it.id == endpointId }
        viewModel.connected.postValue(list)

    }
}

/**
 * Callback to handle receiving payloads
 */
object MessageHandler : PayloadCallback() {
    private val incomingPayloads: MutableMap<Long, Payload> = mutableMapOf()

    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        incomingPayloads[payload.id] = payload
    }

    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        when (update.status) {
            PayloadTransferUpdate.Status.IN_PROGRESS -> {
                Log.i("RECEIVING_PAYLOAD", "Payload transferring... ${update.bytesTransferred}/${update.totalBytes} bytes")
            }
            PayloadTransferUpdate.Status.SUCCESS -> {
                val payload = incomingPayloads[update.payloadId]
                payload?.let { handleCompletePayload(endpointId, payload) }
            }
            PayloadTransferUpdate.Status.FAILURE -> {
                Log.e("RECEIVING_PAYLOAD", "Payload transfer from $endpointId failed")
            }
        }
    }

    private fun handleCompletePayload(endpointId: String, payload: Payload) {
        payload.asBytes()?.let { bytes ->
            Log.i("RECEIVED_PAYLOAD", String(bytes))
            when (val message = messageGson.fromJson(String(bytes), Message::class.java)) {
                is AvatarUpdateMessage -> AvatarUpdateHandler.handle(message)
                is LobbyUpdateMessage -> LobbyUpdateHandler.handle(message)
                is GameBeginMessage -> GameBeginHandler.handle(message)
                is GameEndMessage -> GameEndHandler.handle(message)
                is InGameUpdateMessage -> InGameUpdateHandler.handle(message)
                is YouAreMessage -> YouAreHandler.handle(message)
                else -> Log.e("network", "unknown message type received: $message")
            }
        }
    }
}