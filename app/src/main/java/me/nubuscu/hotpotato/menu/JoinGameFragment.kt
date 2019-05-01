package me.nubuscu.hotpotato.menu


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.connection.AvailableConnectionsViewModel
import me.nubuscu.hotpotato.serviceId

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class JoinGameFragment : Fragment() {

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
        vmAvailableConnections.connections.observe(this, Observer {newGames ->
            if (newGames != null) {
                joinableGamesList.adapter = JoinableGameAdapter(newGames) { info -> connectTo(info)}
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
                    vmAvailableConnections.connections.postValue(
                        vmAvailableConnections.connections.value.apply { this?.add(info) }
                    )
                }

                override fun onEndpointLost(endpointId: String) {
                    Log.d("FOO", "lost endpoint $endpointId")
                }
            }, discoveryOptions)
            .addOnSuccessListener { Log.i("network", "started discovering") }
            .addOnFailureListener { e -> Log.e("network", "failed to start discovering", e) }
    }

    private fun stopDiscovering() {
        Nearby.getConnectionsClient(requireContext()).stopDiscovery()
    }

    private fun connectTo(info: DiscoveredEndpointInfo) {
        stopDiscovering()

    }
}
