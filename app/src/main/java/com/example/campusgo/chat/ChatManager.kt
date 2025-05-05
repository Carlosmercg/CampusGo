package com.example.campusgo.chat

import com.example.campusgo.models.Chat
import com.example.campusgo.models.Mensaje

object ChatManager {
    private val chats = mutableListOf(
        Chat("1", "Juan Pérez", "Hola, ¿tienes el libro disponible?", 1710853400),
        Chat("2", "María López", "Gracias, ya realicé el pago.", 1710853460)
    )

    fun obtenerChats(): List<Chat> = chats

    fun actualizarChat(chatId: String, nuevoMensaje: String, nuevoTimestamp: Long) {
        chats.find { it.id == chatId }?.let {
            val index = chats.indexOf(it)
            chats[index] = it.copy(
                ultimoMensaje = nuevoMensaje,
                timestamp = nuevoTimestamp
            )
        }
    }
    // Agrega al final del archivo ChatManager.kt
    val mensajesPorChat = mutableMapOf<String, MutableList<Mensaje>>()

    fun obtenerMensajes(chatId: String): List<Mensaje> {
        return mensajesPorChat[chatId] ?: emptyList()
    }

    fun agregarMensaje(chatId: String, mensaje: Mensaje) {
        val lista = mensajesPorChat.getOrPut(chatId) { mutableListOf() }
        lista.add(mensaje)
    }

}