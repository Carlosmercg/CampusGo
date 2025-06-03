// com/example/campusgo/models/Usuario.kt
package com.example.campusgo.data.models

data class Usuario(
    val nombre: String = "",
    val apellido: String = "",
    val universidad: String = "",
    val correo: String = "",
    val carrera: String = "",
    val password: String = "",
    val id: String = "",
    val urlCarnet: String = "",
    val urlFotoPerfil: String = "",
    val compras: List<String> = emptyList(),    // IDs de productos comprados
    val ventas: List<String> = emptyList(),     // IDs de productos vendidos
    val chats: List<String> = emptyList()       // IDs de chats activos
)
