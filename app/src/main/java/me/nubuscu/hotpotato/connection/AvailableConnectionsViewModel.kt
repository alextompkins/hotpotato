package me.nubuscu.hotpotato.connection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.nubuscu.hotpotato.model.ActiveClientModel
import me.nubuscu.hotpotato.model.JoinableGameModel

class AvailableConnectionsViewModel: ViewModel() {
    // A list of games that can be joined (hosted by others nearby)
    val joinable: MutableLiveData<MutableList<JoinableGameModel>> by lazy {
        MutableLiveData<MutableList<JoinableGameModel>>()
    }
    //A list of other players that have connected to your device
    val connected: MutableLiveData<MutableList<ActiveClientModel>> by lazy {
        MutableLiveData<MutableList<ActiveClientModel>>()
    }
}

