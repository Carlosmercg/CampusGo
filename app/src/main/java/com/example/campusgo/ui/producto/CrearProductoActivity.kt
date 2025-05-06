package com.example.campusgo.ui.producto

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityCrearProductoBinding

class CrearProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearProductoBinding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categorias = listOf("Selecciona una categoría", "Electrónica", "Libros", "Ropa", "Accesorios")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        binding.spinnerCategoria.adapter = adapter

        binding.btnSeleccionarImagen.setOnClickListener { seleccionarImagen() }
        binding.btnPublicar.setOnClickListener { publicarProducto() }
    }

    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                mostrarImagenSeleccionada(it)
            }
        }
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        seleccionarImagenLauncher.launch(intent)
    }

    private fun mostrarImagenSeleccionada(uri: Uri) {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
        binding.imgProducto.setImageBitmap(bitmap)
    }

    private fun publicarProducto() {
        val nombre = binding.inputNombre.text.toString()
        val precio = binding.inputPrecio.text.toString()
        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val descripcion = binding.inputDescripcion.text.toString()

        if (nombre.isEmpty() || precio.isEmpty() || categoria == "Selecciona una categoría" || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Producto publicado con éxito.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
