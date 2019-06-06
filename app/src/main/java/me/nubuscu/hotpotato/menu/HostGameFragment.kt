package me.nubuscu.hotpotato.menu


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.Strategy
import me.nubuscu.hotpotato.InGameActivity
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.connection.AvailableConnectionsViewModel
import me.nubuscu.hotpotato.connection.ConnectionLifecycleCallback
import me.nubuscu.hotpotato.connection.handler.AvatarUpdateHandler
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.model.dto.AvatarUpdateMessage
import me.nubuscu.hotpotato.model.dto.GameBeginMessage
import me.nubuscu.hotpotato.model.dto.LobbyUpdateMessage
import me.nubuscu.hotpotato.serviceId
import me.nubuscu.hotpotato.util.GameInfoHolder
import me.nubuscu.hotpotato.util.sendToNearbyEndpoints

class HostGameFragment : Fragment() {

    private val username: String
        get() {
            val default = getString(R.string.default_username)
            return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString("username", default)
                ?: default
        }
    private lateinit var vmAvailableConnections: AvailableConnectionsViewModel
    private lateinit var joinedClientsList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vmAvailableConnections = ViewModelProviders.of(requireActivity()).get(AvailableConnectionsViewModel::class.java)
        joinedClientsList = view.findViewById(R.id.joinedClientsList)
        joinedClientsList.layoutManager = LinearLayoutManager(context)
        vmAvailableConnections.connected.observe(this, Observer { newClients ->
            if (newClients != null) {
                joinedClientsList.adapter = ClientAdapter(newClients) { client ->
                    Nearby.getConnectionsClient(requireContext()).disconnectFromEndpoint(client.id)
                    vmAvailableConnections.connected.postValue(
                        vmAvailableConnections.connected.value?.filter { x -> x.id != client.id }?.toMutableList()
                            ?: mutableListOf()
                    )
                    (joinedClientsList.adapter as ClientAdapter).notifyDataSetChanged()
                }
                (joinedClientsList.adapter as ClientAdapter).notifyDataSetChanged()
                notifyClientsOfClients(newClients)
            }
        })
        val startGameButton: Button = view.findViewById(R.id.startGameButton)
        startGameButton.setOnClickListener {
            sendToNearbyEndpoints(
                GameBeginMessage(),
                vmAvailableConnections.connected.value?.map { it.id } ?: listOf(),
                requireContext())
            GameInfoHolder.instance.endpoints.addAll(
                vmAvailableConnections.connected.value?.toMutableSet() ?: mutableSetOf()
            )
            val intent = Intent(requireContext(), InGameActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        clearConnections()
        startAdvertising()
        GameInfoHolder.instance.isHost = true
        AvatarUpdateHandler.addExtraHandler(avatarUpdateHandler)
    }

    override fun onPause() {
        super.onPause()
        stopAdvertising()
        AvatarUpdateHandler.removeExtraHandler(avatarUpdateHandler)
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(requireContext())
            .startAdvertising(
                username,
                serviceId,
                ConnectionLifecycleCallback(vmAvailableConnections),
                advertisingOptions
            ).addOnSuccessListener {
                Log.i("network", "started advertising as host")
            }.addOnFailureListener { e ->
                Log.e("network", "failed to start advertising as host", e)
            }

    }

    private fun stopAdvertising() {
        Nearby.getConnectionsClient(requireContext()).stopAdvertising()
        Log.i("network", "stopped advertising as host")
    }

    private fun clearConnections() {
        GameInfoHolder.instance.endpoints = mutableSetOf()
        Nearby.getConnectionsClient(requireContext()).stopAllEndpoints()
        vmAvailableConnections.connected.postValue(mutableListOf())
    }

    /**
     * Sends a message to all clients, informing them of all the members of the current lobby
     */
    private fun notifyClientsOfClients(members: MutableList<ClientDetailsModel>) {
        val content = LobbyUpdateMessage(members)
        sendToNearbyEndpoints(content, members.map { it.id }, requireContext())
    }

    // MESSAGE HANDLERS
    private val avatarUpdateHandler = { message: AvatarUpdateMessage ->
        // Forward the avatarUpdateMessage to all endpoints apart from ourselves and the one who sent it
        val info = GameInfoHolder.instance
        val otherEndpoints = info.endpoints
            .map { it.id }
            .filter { it != info.myEndpointId && it != message.endpointId }
        if (otherEndpoints.isNotEmpty()) {
            sendToNearbyEndpoints(message, otherEndpoints, requireContext())
        }

        // Update the icon for the player
        val clientAdapter = joinedClientsList.adapter as ClientAdapter
        val changedIndex = clientAdapter.clients.indexOfFirst { it.id == message.endpointId }
        clientAdapter.notifyItemChanged(changedIndex)
    }
}
