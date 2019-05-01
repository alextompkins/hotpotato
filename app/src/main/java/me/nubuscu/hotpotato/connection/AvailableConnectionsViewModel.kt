package me.nubuscu.hotpotato.connection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

class AvailableConnectionsViewModel: ViewModel() {
    val connections: MutableLiveData<MutableList<DiscoveredEndpointInfo>> by lazy {
        MutableLiveData<MutableList<DiscoveredEndpointInfo>>()
    }
}

