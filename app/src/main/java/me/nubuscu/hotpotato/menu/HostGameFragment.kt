package me.nubuscu.hotpotato.menu


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import me.nubuscu.hotpotato.R
import me.nubuscu.hotpotato.connection.ConnectionLifecycleCallback
import me.nubuscu.hotpotato.serviceId

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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_game, container, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        try {
            if (isVisibleToUser) {
                startAdvertising()
            } else {
                stopAdvertising()
            }
        } catch (e: IllegalStateException) {
            //oops
        }
    }
    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(requireContext())
            .startAdvertising(
                hostNickname,
                serviceId,
                ConnectionLifecycleCallback,
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

}
