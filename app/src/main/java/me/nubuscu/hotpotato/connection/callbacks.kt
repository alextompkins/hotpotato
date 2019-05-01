package me.nubuscu.hotpotato.connection

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.PayloadCallback
import me.nubuscu.hotpotato.util.DataHolder

/**
 * Callback used to handle lifecycle events. Called when advertising to host a game
 */
object ConnectionLifecycleCallback : ConnectionLifecycleCallback() {
    override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
        Nearby.getConnectionsClient(DataHolder.instance.context.get()!!).acceptConnection(endpointId, PayloadCallback)
    }

    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        when(result.status.statusCode) {
            ConnectionsStatusCodes.STATUS_OK -> Log.d("FOO", "hey, that's pretty good")
            ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Log.d("FOO", "oh no")
            ConnectionsStatusCodes.STATUS_ERROR -> Log.d("FOO", "that's not good")
            else -> {
                Log.d("FOO", "unknown connection code: ${result.status.statusCode}")
            }
        }
    }

    override fun onDisconnected(endpointId: String) {
        Log.i("network", "disconnected from endpoint with id: $endpointId")
    }
}

/**
 * Callback to handle receiving payloads
 */
object PayloadCallback: PayloadCallback() {
    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}