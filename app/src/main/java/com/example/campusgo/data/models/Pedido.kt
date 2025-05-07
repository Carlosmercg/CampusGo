package com.example.campusgo.data.models

import com.google.firebase.Timestamp

data class Pedido(
    val id: String,
    val compradorId: String,
    val vendedorId: String,
    val productos: List<Producto>,
    val estado: String,
    val fecha: Timestamp,
    val direccion: String,
    val latVendedor: Double,
    val longVendedor: Double,
    val metodoPago: String
)
