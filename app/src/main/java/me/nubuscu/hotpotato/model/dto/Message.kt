package me.nubuscu.hotpotato.model.dto

/**
 * Note: @param type must match that which is used in the gson adapter for deserialization to work properly
 */
open class Message(val type: String)