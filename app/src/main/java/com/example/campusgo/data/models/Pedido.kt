package com.example.campusgo.data.models

import android.os.Parcel
import com.google.firebase.Timestamp

data class Pedido(
    val id: String,
    val compradorId: String,
    val vendedorId: String,
    val productos: List<Producto>,
    val estado: String = "",
    val fecha: Timestamp,
    val direccion: String,
    val latvendedor: Double,
    val longvendedor: Double,
    val metodoPago: String
) {
    constructor() : this("", "", "", emptyList(), "", Timestamp.now(), "", 0.0, 0.0, "")
}
