package me.nubuscu.hotpotato.connection

import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.nubuscu.hotpotato.connection.handler.LobbyUpdateHandler
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.model.dto.LobbyUpdateMessage
import me.nubuscu.hotpotato.model.dto.Message
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.serialization.RuntimeTypeAdapterFactory

/**
 * Callback used to handle lifecycle events. Called when advertising to host a game
 */
class ConnectionLifecycleCallback(private val viewModel: AvailableConnectionsViewModel) :
    ConnectionLifecycleCallback() {
    private val tentativeConnections = mutableSetOf<ClientDetailsModel>()

    override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
        tentativeConnections.add(ClientDetailsModel(endpointId, connectionInfo.endpointName))
        Nearby.getConnectionsClient(DataHolder.instance.context.get()!!).acceptConnection(endpointId, PayloadCallback)
    }

    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        val client = tentativeConnections.find { it.id == endpointId }
        client?.let { tentativeConnections.removeAll { it.id == endpointId } }
        when (result.status.statusCode) {
            ConnectionsStatusCodes.STATUS_OK -> {
                Log.d("FOO", "hey, that's pretty good")
                val list = viewModel.connected.value ?: mutableListOf()
                list.add(client ?: ClientDetailsModel(endpointId, "Unknown"))
                viewModel.connected.postValue(list)
            }
            ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Log.d("FOO", "oh no")
            ConnectionsStatusCodes.STATUS_ERROR -> Log.d("FOO", "that's not good")
            else -> {
                Log.d("FOO", "unknown connection code: ${result.status.statusCode}")
            }
        }
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
object PayloadCallback : PayloadCallback() {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(
            RuntimeTypeAdapterFactory
                .of(Message::class.java, "messageType")
                .registerSubtype(LobbyUpdateMessage::class.java)
        )
        .create()

    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        payload.asBytes()?.let { bytes ->
            when (val message = gson.fromJson(String(bytes), Message::class.java)) {
                is LobbyUpdateMessage -> LobbyUpdateHandler().handle(message)
                else -> Log.e("network", "unknown message type received: $message")
            }
        }
    }

    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        //bytes payloads are sent in one chunk, no need to wait for this
        //do nothing
    }
}