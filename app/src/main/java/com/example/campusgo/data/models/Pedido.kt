package com.example.campusgo.data.models

data class Pedido(
    val id: String,
    val compradorId: String,
    val vendedorId: String,
    val productos: List<Producto>,
    val estado: String,
    val fecha: Long,
    val direccion: String,
    val latVendedor: Double,
    val longVendedor: Double
)
