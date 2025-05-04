package com.example.campusgo.models

data class Pedido(
    val id: String,
    val compradorId: String,
    val vendedorId: String,
    val productos: List<Producto>,
    val estado: String,
    val fecha: Long
)
