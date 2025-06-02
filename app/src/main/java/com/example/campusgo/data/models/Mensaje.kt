package com.example.campusgo.data.models

data class Mensaje(
    val contenido: String = "",
    val emisor: String = "",
    val receptor: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val esEnviado: Boolean = false  // Solo se usa para UI
)
