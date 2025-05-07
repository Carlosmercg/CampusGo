package com.example.campusgo.producto

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isNotEmpty
import com.example.campusgo.databinding.ActivityCrearProductoBinding
import com.example.campusgo.models.Categoria
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.util.GeoPoint

class CrearProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearProductoBinding
    private var imageUri: Uri? = null
    private var imageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }
        binding.guardar.setOnClickListener {
            guardarProducto()
        }

        binding.fotoProducto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            seleccionarImagenLauncher.launch(intent)

        }


    }

    private fun guardarProducto() {
        if(binding.nombre.text.isNotEmpty() && binding.descripcion.text.isNotEmpty() && binding.precio.text.isNotEmpty() && binding.categoria.isNotEmpty() && imageChanged){

        }
        else {
            Toast.makeText(baseContext, "Debe llenar todos los campos", Toast.LENGTH_SHORT).show()
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
        binding.fotoProducto.setImageBitmap(bitmap)
        imageChanged = true
    }

    private fun inicializarSpinner(){
        val db = FirebaseFirestore.getInstance()
        var categorias = mutableListOf<String>()
        db.collection("Categorías")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.getString("nombre")?.let { categorias.add(it) }
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.categoria.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al leer pedidos", exception)
            }
    }
}