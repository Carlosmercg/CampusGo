// com/example/campusgo/models/Usuario.kt
package com.example.campusgo.models

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val universidad: String = "",
    val password: String = "",
    val correo: String = "",
    val fotoPerfilUrl: String = "",     // URL a Firebase Storage
    val fotoCarnetUrl: String = "",     // URL a Firebase Storage
    val compras: List<String> = emptyList(),    // IDs de productos comprados
    val ventas: List<String> = emptyList(),     // IDs de productos vendidos
    val chats: List<String> = emptyList()       // IDs de chats activos
)
