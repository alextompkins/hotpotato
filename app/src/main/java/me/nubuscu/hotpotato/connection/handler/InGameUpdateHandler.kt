package me.nubuscu.hotpotato.connection.handler

import me.nubuscu.hotpotato.model.dto.InGameUpdateMessage

class InGameUpdateHandler: PayloadHandler<InGameUpdateMessage> {
    override fun handle(message: InGameUpdateMessage) {
        /*
        if (amHost && message.dest != me) {
            send(message, message.dest)
        } else {
            toggle to the bit where I'm playing the game
        }
         */
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}