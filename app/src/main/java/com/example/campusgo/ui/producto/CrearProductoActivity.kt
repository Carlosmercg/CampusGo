package com.example.campusgo.ui.producto

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isNotEmpty
import com.example.campusgo.data.models.Categoria
import com.example.campusgo.data.models.Producto
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.databinding.ActivityCrearProductoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.osmdroid.util.GeoPoint

class CrearProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearProductoBinding
    private var imageUri: Uri? = null
    private var imageChanged = false
    private val TAG = "CrearProductoActivity"

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

        inicializarSpinner()

    }

    private fun guardarProducto() {
        val nombre = binding.nombre.text.toString()
        val descripcion = binding.descripcion.text.toString()
        val precioTexto = binding.precio.text.toString()
        val nombreCategoria = binding.categoria.selectedItem.toString()

        if (!validarProducto(nombre, descripcion, precioTexto, nombreCategoria, imageChanged)) return

        val precio = precioTexto.toDouble()
        val vendedorId = Firebase.auth.currentUser?.uid ?: return

        obtenerIdCategoriaPorNombre(nombreCategoria) { categoriaId ->
            if (categoriaId == null) {
                Toast.makeText(this, "No se encontró la categoría seleccionada", Toast.LENGTH_SHORT).show()
                return@obtenerIdCategoriaPorNombre
            }

            val manejadorImagenes = ManejadorImagenesAPI(this)
            imageUri?.let { uri ->
                manejadorImagenes.subirImagen(uri) { urlImagen ->
                    if (urlImagen == null) {
                        Toast.makeText(this, "❌ Error al subir imagen", Toast.LENGTH_SHORT).show()
                        return@subirImagen
                    }

                    buscarNombre(vendedorId) { vendedorNombre ->
                        val db = FirebaseFirestore.getInstance()
                        val productoRef = db.collection("Productos").document()

                        // Crear el producto como objeto
                        val producto = Producto(
                            id = productoRef.id,
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precio,
                            categoriaId = categoriaId,
                            imagenUrl = urlImagen,
                            vendedorId = vendedorId,
                            vendedorNombre = vendedorNombre
                        )

                        productoRef.set(producto)
                            .addOnSuccessListener {
                                Log.d("Firestore", "✅ Producto agregado con ID: ${producto.id}")
                                Toast.makeText(this, "Producto agregado con éxito", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "❌ Error al guardar producto", e)
                                Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
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
                Log.e("Firestore", "Error al leer productos", exception)

            }
    }

    private fun validarProducto(
        nombre: String,
        descripcion: String,
        precioTexto: String,
        categoria: String,
        imageChanged: Boolean
    ): Boolean {
        return when {
            nombre.isEmpty() || descripcion.isEmpty() || precioTexto.isEmpty() || categoria.isEmpty() -> {
                mostrar("Completa todos los campos")
                false
            }
            precioTexto.toDoubleOrNull() == null -> {
                mostrar("Precio Invalido")
                false
            }
            !imageChanged -> {
                mostrar("Debes Subir una imagen de producto")
                false
            }
            else -> true
        }
    }


    fun buscarNombre(Id: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .document(Id)
            .get()
            .addOnSuccessListener { usuarioDoc ->
                val nombre = usuarioDoc.getString("nombre") ?: "Nombre desconocido"
                callback(nombre)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener nombre", e)
                callback("Nombre desconocido")

            }
    }

    private fun obtenerIdCategoriaPorNombre(nombreCategoria: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Categorías")
            .whereEqualTo("nombre", nombreCategoria)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val categoriaId = result.documents[0].id
                    callback(categoriaId)
                } else {
                    Log.w("Firestore", "❗ Categoría no encontrada: $nombreCategoria")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "❌ Error al buscar ID de categoría", exception)
                callback(null)
            }
    }



    private fun mostrar(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Log.d(TAG, msg)
    }
}