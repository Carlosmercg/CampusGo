package com.example.campusgo.data.models

data class Mensaje(
    val contenido: String,
    val esEnviado: Boolean // true si el usuario lo envió, false si lo recibió
)
