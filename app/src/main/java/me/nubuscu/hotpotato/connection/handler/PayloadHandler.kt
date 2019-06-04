package me.nubuscu.hotpotato.connection.handler

import me.nubuscu.hotpotato.model.dto.Message

abstract class PayloadHandler<T: Message> {
    private val extraHandlers: MutableList<(T) -> Unit> = mutableListOf()

    internal open fun handle(message: T) {
        extraHandlers.forEach { it(message) }
    }

    fun addExtraHandler(handler: (T) -> Unit) {
        extraHandlers.add(handler)
    }

    fun removeExtraHandler(handler: (T) -> Unit) {
        extraHandlers.remove(handler)
    }
}
