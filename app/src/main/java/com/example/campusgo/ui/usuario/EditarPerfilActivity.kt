package com.example.campusgo.ui.usuario

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.R
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.databinding.ActivityEditarPerfilBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private var imageUri: Uri? = null
    private lateinit var usuario : Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()
        val userId = Firebase.auth.currentUser?.uid
        val usuarioRef = db.collection("usuarios").document(userId ?: "")

        usuarioRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                usuario = documentSnapshot.toObject(Usuario::class.java)!!
                ManejadorImagenesAPI.mostrarImagenDesdeUrl(
                    usuario.urlFotoPerfil,
                    binding.profilePicture,
                    this,
                    R.drawable.ic_profile,
                )
            }
        }


        binding.back.setOnClickListener {
            finish()
        }

        binding.guardar.setOnClickListener {
            actualizarperfil()
            finish()
        }

        binding.profilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            seleccionarImagenLauncher.launch(intent)
        }

    }

    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                mostrarImagenSeleccionada(it)
            }
        }
    }

    private fun mostrarImagenSeleccionada(uri: Uri) {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
        binding.profilePicture.setImageBitmap(bitmap)
    }

    private fun actualizarperfil() {
        val userId = Firebase.auth.currentUser?.uid
        val uri = imageUri

        if (uri != null && userId != null) {
            val manejadorImagenes = ManejadorImagenesAPI(this)

            manejadorImagenes.subirImagen(uri) { urlImagen ->
                if (urlImagen != null) {
                    val db = FirebaseFirestore.getInstance()
                    val usuarioRef = db.collection("usuarios").document(userId)

                    usuarioRef.update("urlFotoPerfil", urlImagen)
                        .addOnSuccessListener {
                            Log.d("EditarPerfil", "✅ Foto de perfil actualizada")
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            Toast.makeText(
                                this,
                                "❌ Error al guardar los cambios",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    if (binding.nombres.text.toString().isNotEmpty()) {
                        usuarioRef.update("nombre", binding.nombres.text.toString())
                            .addOnSuccessListener {
                                Log.d("EditarPerfil", "✅ Nombre actualizado")
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            }
                    }

                    if (binding.apellidos.text.toString().isNotEmpty()) {
                        usuarioRef.update("apellido", binding.apellidos.text.toString())
                            .addOnSuccessListener {
                                Log.d("EditarPerfil", "✅ Apellido actualizado")
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            }
                    }

                    if (binding.email.text.toString().isNotEmpty()) {
                        usuarioRef.update("correo", binding.email.text.toString())
                            .addOnSuccessListener {
                                Log.d("EditarPerfil", "✅ Correo actualizado")
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            }
                    }

                    if (binding.carrera.text.toString().isNotEmpty()) {
                        usuarioRef.update("carrera", binding.carrera.text.toString())
                            .addOnSuccessListener {
                                Log.d("EditarPerfil", "✅ Carrera actualizada")
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            }
                    }

                    if (binding.telefono.text.toString().isNotEmpty()) {
                        usuarioRef.update("telefono", binding.telefono.text.toString())
                            .addOnSuccessListener {
                                Log.d("EditarPerfil", "✅ Telefono actualizado")
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            }
                    }

                    if (binding.spinnerCiudad.selectedItem.toString().isNotEmpty()) {
                        usuarioRef.update("ciudad", binding.spinnerCiudad.selectedItem.toString())
                            .addOnSuccessListener {
                                Log.d("EditarPerfil", "✅ Ciudad actualizada")
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            }

                    }

                    if (binding.spinnerUniversidad.selectedItem.toString().isNotEmpty()) {
                        usuarioRef.update("universidad", binding.spinnerUniversidad.selectedItem.toString())
                            .addOnSuccessListener {
                                Log.d("EditarPerfil", "✅ Universidad actualizada")
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            }
                    }
                } else {
                    Toast.makeText(this, "❌ No se pudo subir la imagen", Toast.LENGTH_SHORT).show()
                }
            }

        } else {

            finish()
        }
    }

}