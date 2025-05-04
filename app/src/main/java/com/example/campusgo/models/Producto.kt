package com.example.campusgo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val id: String = "",
    val nombre: String = "",
    val vendedorId: String = "",           // ← ID real del Usuario vendedor
    val precio: Double = 0.0,
    val imagenUrl: String = ""             // URL en Firebase Storage
) : Parcelable
