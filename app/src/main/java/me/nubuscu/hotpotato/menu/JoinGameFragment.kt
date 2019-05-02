package me.nubuscu.hotpotato.menu


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import me.nubuscu.hotpotato.JoinableGameModel
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.connection.AvailableConnectionsViewModel
import me.nubuscu.hotpotato.connection.ConnectionLifecycleCallback
import me.nubuscu.hotpotato.serviceId


class JoinGameFragment : Fragment() {
    var hostNickname = "Tony the Tea Towel Man" //TODO load this from preferences

    private lateinit var vmAvailableConnections: AvailableConnectionsViewModel
    private lateinit var joinableGamesList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_join_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vmAvailableConnections = ViewModelProviders.of(requireActivity()).get(AvailableConnectionsViewModel::class.java)
        joinableGamesList = view.findViewById(R.id.joinableGamesList)
        joinableGamesList.layoutManager = LinearLayoutManager(context)
        vmAvailableConnections.connections.observe(this, Observer { newGames ->
            Log.d("FOO", "vm observer triggered")
            if (newGames != null) {
                Log.d("FOO", "$newGames")
                joinableGamesList.adapter = JoinableGameAdapter(newGames) { info -> connectTo(info) }
                (joinableGamesList.adapter as JoinableGameAdapter).notifyDataSetChanged()
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        try {
            if (isVisibleToUser) {
                startDiscovering()
            } else {
                stopDiscovering()
            }
        } catch (e: IllegalStateException) {
            //oops
        }
    }

    private fun startDiscovering() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(requireContext())
            .startDiscovery(serviceId, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    Log.d("FOO", "found endpoint $endpointId, info: ${info.endpointName} -- ${info.serviceId}")
                    val newGames = vmAvailableConnections.connections.value ?: mutableListOf()
                    newGames.add(JoinableGameModel(endpointId, info.endpointName, info.serviceId))
                    vmAvailableConnections.connections.postValue(newGames)
                }

                override fun onEndpointLost(endpointId: String) {
                    Log.i("network", "lost endpoint $endpointId")
                    val newGames = vmAvailableConnections.connections.value ?: mutableListOf()
                    for (game in newGames) {

                    }
                    newGames.retainAll { it.endpointId != endpointId }
                    vmAvailableConnections.connections.postValue(newGames)
                }
            }, discoveryOptions)
            .addOnSuccessListener { Log.i("network", "started discovering") }
            .addOnFailureListener { e -> Log.e("network", "failed to start discovering", e) }
    }

    private fun stopDiscovering() {
        //empty the list for next time we start searching
        try {
            vmAvailableConnections.connections.postValue(mutableListOf())
        } catch (e: UninitializedPropertyAccessException) {
            //this happens once on startup
        }
        Nearby.getConnectionsClient(requireContext()).stopDiscovery()
    }

    private fun connectTo(game: JoinableGameModel) {
        Log.d("FOO", "connectTo triggered")
        Nearby.getConnectionsClient(requireContext())
            .requestConnection(hostNickname, game.endpointId, ConnectionLifecycleCallback)
            .addOnSuccessListener {
                Log.i("network", "successfully requested connection to ${game.endpointId}")
                stopDiscovering()
            }.addOnFailureListener { Log.i("network", "failed to connect to ${game.endpointId}") }
    }
}
