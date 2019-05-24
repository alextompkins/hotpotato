package me.nubuscu.hotpotato.menu


//import me.nubuscu.hotpotato.connection.ConnectionLifecycleCallback
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
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.connection.AvailableConnectionsViewModel
import me.nubuscu.hotpotato.connection.ConnectionLifecycleCallback
import me.nubuscu.hotpotato.model.dto.LobbyUpdateMessage
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.serviceId
import me.nubuscu.hotpotato.util.serialization.messageGson

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class HostGameFragment : Fragment() {

    var hostNickname = "Jimothy" //TODO load this from preferences
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
                joinedClientsList.adapter = ClientAdapter(newClients) { /*TODO click listener goes here*/ }
                (joinedClientsList.adapter as ClientAdapter).notifyDataSetChanged()
                notifyClientsOfClients(newClients)
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        try {
            if (isVisibleToUser) {
                startAdvertising()
            } else {
                stopAdvertising()
                clearConnections()
            }
        } catch (e: IllegalStateException) {
            //oops..?
        }
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(requireContext())
            .startAdvertising(
                hostNickname,
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
        Nearby.getConnectionsClient(requireContext()).stopAllEndpoints()
    }

    /**
     * Sends a message to all clients, informing them of all the members of the current lobby
     */
    private fun notifyClientsOfClients(members: MutableList<ClientDetailsModel>) {
        val content = LobbyUpdateMessage(members)
        val payload = Payload.fromBytes(messageGson.toJson(content).toByteArray())
        Nearby.getConnectionsClient(requireContext()).sendPayload(members.map { it.id }, payload)
    }
}


