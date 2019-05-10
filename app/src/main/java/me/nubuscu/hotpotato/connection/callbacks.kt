package me.nubuscu.hotpotato.connection

import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.PayloadCallback
import me.nubuscu.hotpotato.model.ActiveClientModel
import me.nubuscu.hotpotato.util.DataHolder

/**
 * Callback used to handle lifecycle events. Called when advertising to host a game
 */
class ConnectionLifecycleCallback(private val viewModel: AvailableConnectionsViewModel) :
    ConnectionLifecycleCallback() {
    private val tentativeConnections = mutableSetOf<ActiveClientModel>()

    override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
        tentativeConnections.add(ActiveClientModel(endpointId, connectionInfo.endpointName))
        Nearby.getConnectionsClient(DataHolder.instance.context.get()!!).acceptConnection(endpointId, PayloadCallback)
    }

    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        val client = tentativeConnections.find { it.id == endpointId }
        client?.let { tentativeConnections.removeAll { it.id == endpointId } }
        when (result.status.statusCode) {
            ConnectionsStatusCodes.STATUS_OK -> {
                Log.d("FOO", "hey, that's pretty good")
                val list = viewModel.connected.value ?: mutableListOf()
                list.add(client ?: ActiveClientModel(endpointId, "Unknown"))
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
    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}