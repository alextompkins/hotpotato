package me.nubuscu.hotpotato.connection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import me.nubuscu.hotpotato.JoinableGameModel

class AvailableConnectionsViewModel: ViewModel() {
    val connections: MutableLiveData<MutableList<JoinableGameModel>> by lazy {
        MutableLiveData<MutableList<JoinableGameModel>>()
    }
}

