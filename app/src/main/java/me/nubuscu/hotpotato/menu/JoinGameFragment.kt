package me.nubuscu.hotpotato.menu


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.connection.AvailableConnectionsViewModel
import me.nubuscu.hotpotato.connection.ConnectionLifecycleCallback
import me.nubuscu.hotpotato.connection.handler.AvatarUpdateHandler
import me.nubuscu.hotpotato.model.JoinableGameModel
import me.nubuscu.hotpotato.model.dto.AvatarUpdateMessage
import me.nubuscu.hotpotato.serviceId
import me.nubuscu.hotpotato.util.GameInfoHolder

class JoinGameFragment : Fragment() {

    private val username: String
        get() {
            val default = getString(R.string.default_username)
            return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString("username", default)
                ?: default
        }
    private lateinit var vmAvailableConnections: AvailableConnectionsViewModel
    private lateinit var frameLayout: FrameLayout
    private lateinit var joinableGamesList: RecyclerView
    private lateinit var playersList: RecyclerView
    private lateinit var joiningProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_join_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        joiningProgressBar = view.findViewById(R.id.joiningProgressBar)
        frameLayout = view.findViewById(R.id.frameLayout)
        joinableGamesList = view.findViewById(R.id.joinableGamesList)
        joinableGamesList.layoutManager = LinearLayoutManager(context)
        playersList = view.findViewById(R.id.playersList)
        playersList.layoutManager = LinearLayoutManager(context)

        vmAvailableConnections = ViewModelProviders.of(requireActivity()).get(AvailableConnectionsViewModel::class.java)
        vmAvailableConnections.joinable.observe(this, Observer { newGames ->
            if (newGames != null) {
                joinableGamesList.adapter = JoinableGameAdapter(newGames) { info -> connectTo(info) }
                (joinableGamesList.adapter as JoinableGameAdapter).notifyDataSetChanged()
            }
        })
        vmAvailableConnections.connected.observe(this, Observer { connections ->
            if (connections != null) {
                playersList.adapter = ClientAdapter(connections)
                (playersList.adapter as ClientAdapter).notifyDataSetChanged()
            }
        })
        switchTo(joinableGamesList)
    }

    override fun onResume() {
        super.onResume()
        vmAvailableConnections.joinable.postValue(mutableListOf())
        startDiscovering()
        GameInfoHolder.instance.isHost = false
        AvatarUpdateHandler.addExtraHandler(avatarUpdateHandler)
    }

    override fun onPause() {
        stopDiscovering()
        super.onPause()
        AvatarUpdateHandler.removeExtraHandler(avatarUpdateHandler)
    }

    private fun switchTo(list: RecyclerView) {
        frameLayout.removeAllViews()
        frameLayout.addView(list)
    }

    private fun startDiscovering() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(requireContext())
            .startDiscovery(serviceId, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    Log.d("FOO", "found endpoint $endpointId, info: ${info.endpointName} -- ${info.serviceId}")
                    val newGames = vmAvailableConnections.joinable.value ?: mutableListOf()
                    newGames.add(
                        JoinableGameModel(
                            endpointId,
                            info.endpointName,
                            info.serviceId
                        )
                    )
                    vmAvailableConnections.joinable.postValue(newGames)
                }

                override fun onEndpointLost(endpointId: String) {
                    Log.i("network", "lost endpoint $endpointId")
                    val newGames = vmAvailableConnections.joinable.value ?: mutableListOf()
                    for (game in newGames) {

                    }
                    newGames.retainAll { it.endpointId != endpointId }
                    vmAvailableConnections.joinable.postValue(newGames)
                }
            }, discoveryOptions)
            .addOnSuccessListener { Log.i("network", "started discovering") }
            .addOnFailureListener { e -> Log.e("network", "failed to start discovering", e) }
    }

    private fun stopDiscovering() {
        //empty the list for next time we start searching
        try {
            vmAvailableConnections.joinable.postValue(mutableListOf())
        } catch (e: UninitializedPropertyAccessException) {
            //this happens once on startup
        }
        Nearby.getConnectionsClient(requireContext()).stopDiscovery()
    }

    private fun connectTo(game: JoinableGameModel) {
        Log.d("FOO", "connectTo triggered")
        joiningProgressBar.visibility = View.VISIBLE
        Nearby.getConnectionsClient(requireContext())
            .requestConnection(username, game.endpointId, ConnectionLifecycleCallback(vmAvailableConnections))
            .addOnSuccessListener {
                Log.i("network", "successfully requested connection to ${game.endpointId}")
                stopDiscovering()
                joiningProgressBar.visibility = View.GONE
                switchTo(playersList)
            }.addOnFailureListener { e ->
                Log.e("network", "failed to connect to ${game.endpointId}", e)
                joiningProgressBar.visibility = View.GONE
            }
    }

    // MESSAGE HANDLERS
    private val avatarUpdateHandler = { message: AvatarUpdateMessage ->
        // Update the icon for the player
        val playersAdapter = playersList.adapter as ClientAdapter
        val changedIndex = playersAdapter.clients.indexOfFirst { it.id == message.endpointId }
        playersAdapter.notifyItemChanged(changedIndex)
    }
}
