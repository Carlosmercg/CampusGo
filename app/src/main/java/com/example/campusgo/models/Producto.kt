package com.example.campusgo.models

data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String // Se usa una URL remota
)
