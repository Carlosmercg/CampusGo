package com.example.campusgo.data.models
data class Chat(
    val id: String = "",
    val uidReceptor: String = "",
    val nombreUsuario: String = "",
    val apellidoUsuario: String = "",
    val urlFotoPerfil: String = "",
    val ultimoMensaje: String = "",
    val timestamp: Long = 0L
)

