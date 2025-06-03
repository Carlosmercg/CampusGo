package com.example.campusgo.data.models

data class Chat(
    val id: String,
    val nombreUsuario: String,
    val ultimoMensaje: String,
    val timestamp: Long,
    val uidReceptor: String = "",
    val urlFotoPerfil: String = ""
)
