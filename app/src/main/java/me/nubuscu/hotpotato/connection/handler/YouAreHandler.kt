package me.nubuscu.hotpotato.connection.handler

import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.model.dto.YouAreMessage
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.GameInfoHolder
import java.io.File

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
            val myDetails = ClientDetailsModel(myEndpointId!!, myName)
            endpoints.add(myDetails)

            val avatarData = getOwnAvatar()
            myDetails.profilePicture = avatarData
        }
    }

    private fun getOwnAvatar(): ByteArray? {
        val avatarUri = File(DataHolder.instance.context.get()!!.filesDir, "avatar").toUri()
        return if (avatarUri.toFile().exists()) avatarUri.toFile().readBytes() else null
    }
}
