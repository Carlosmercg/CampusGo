package com.example.campusgo.data.models

import java.io.Serializable

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val vendedorId: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = "",
    val vendedorNombre: String = "",
    val descripcion: String = ""
) : Serializable