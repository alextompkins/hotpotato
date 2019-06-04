package me.nubuscu.hotpotato.connection.handler

import android.util.Log
import androidx.preference.PreferenceManager
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.model.dto.YouAreMessage
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.GameInfoHolder

object YouAreHandler : PayloadHandler<YouAreMessage>() {
    override fun handle(message: YouAreMessage) {
        super.handle(message)

        Log.d("iam", "I am ${message.endpoint}")
        GameInfoHolder.instance.apply {
            myEndpointId = message.endpoint
            val default = "new player"
            val myName = PreferenceManager
                .getDefaultSharedPreferences(DataHolder.instance.context.get())
                .getString("username", default)
                ?: default
            endpoints.add(ClientDetailsModel(myEndpointId!!, myName))
        }
    }
}
