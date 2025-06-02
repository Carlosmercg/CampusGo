package com.example.campusgo.data.repository

import android.util.Log
import com.example.campusgo.data.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class UsuarioRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val TAG = "UsuarioRepository"
    }

    /**
     * Guarda un nuevo usuario en Firestore bajo el documento con ID igual al uid proporcionado.
     * Si ya existe, se sobrescribirá completamente.
     */
    fun registrarUsuario(
        usuario: Usuario,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("usuarios")
            .document(usuario.id) // ID explícito (uid de Firebase Authentication)
            .set(usuario)
            .addOnSuccessListener {
                Log.d(TAG, "Usuario registrado correctamente con ID: ${usuario.id}")
                onSuccess()
            }
            .addOnFailureListener {
                Log.e(TAG, "Error al registrar usuario: ${it.message}", it)
                onError(it.message ?: "Error desconocido al registrar usuario")
            }
    }

    /**
     * Obtiene un usuario por su UID.
     */
    fun obtenerUsuario(
        uid: String,
        onSuccess: (Usuario) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val usuario = document.toObject(Usuario::class.java)
                if (usuario != null) {
                    Log.d(TAG, "Usuario obtenido: $usuario")
                    onSuccess(usuario)
                } else {
                    Log.w(TAG, "Usuario no encontrado con ID: $uid")
                    onError("Usuario no encontrado")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Error al obtener usuario: ${it.message}", it)
                onError(it.message ?: "Error al leer usuario")
            }
    }
}
