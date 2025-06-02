package com.example.campusgo.data.models

data class Mensaje(
    val contenido: String = "",
    val emisor: String = "",
    val receptor: String = "",
    val timestamp: Long = 0,
    @Transient var esEnviado: Boolean = false
)

