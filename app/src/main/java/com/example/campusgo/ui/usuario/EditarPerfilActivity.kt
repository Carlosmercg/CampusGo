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
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.databinding.ActivityEditarPerfilBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        binding.guardar.setOnClickListener {
            Actualizarperfil()
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

    private fun Actualizarperfil() {
        val userId = Firebase.auth.currentUser?.uid
        val uri = imageUri

        if (uri != null && userId != null) {
            val manejadorImagenes = ManejadorImagenesAPI(this)

            manejadorImagenes.subirImagen(uri) { urlImagen ->
                if (urlImagen != null) {
                    val db = FirebaseFirestore.getInstance()
                    val usuarioRef = db.collection("usuarios").document(userId)

                    usuarioRef.update("fotoPerfilUrl", urlImagen)
                        .addOnSuccessListener {
                            Toast.makeText(this, "✅ Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditarPerfil", "❌ Error al actualizar Firestore", e)
                            Toast.makeText(this, "❌ Error al guardar los cambios", Toast.LENGTH_SHORT).show()
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