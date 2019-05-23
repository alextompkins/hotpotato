package me.nubuscu.hotpotato.connection.handler

import me.nubuscu.hotpotato.model.dto.Message

interface PayloadHandler<T: Message> {

    fun handle(message: T)
}