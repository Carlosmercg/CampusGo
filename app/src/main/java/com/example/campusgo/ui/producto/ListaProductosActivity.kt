package com.example.campusgo.ui.producto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.ui.adapters.ProductoAdapter
import com.example.campusgo.databinding.ActivityListaProductosBinding
import com.example.campusgo.data.models.Producto
import com.google.firebase.firestore.FirebaseFirestore

class ListaProductosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaProductosBinding
    private lateinit var adapter: ProductoAdapter
    private val productos = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoriaId = intent.getStringExtra("categoriaId") ?: ""
        val categoriaNombre = intent.getStringExtra("categoriaNombre") ?: getString(R.string.home_title)

        cargarProductosDesdeFirebase(categoriaId)

        binding.toolbarLista.title = categoriaNombre
        setSupportActionBar(binding.toolbarLista)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarLista.setNavigationOnClickListener { finish() }


        adapter = ProductoAdapter(productos) { producto ->
            Intent(this, DetalleProductoActivity::class.java).apply {
                putExtra("producto", producto)
            }.also(::startActivity)
        }

        binding.rvProductos.layoutManager = LinearLayoutManager(this)
        binding.rvProductos.adapter = adapter
    }

    private fun cargarProductosDesdeFirebase(categoriaId: String) {
        FirebaseFirestore.getInstance()
            .collection("Productos")
            .whereEqualTo("CategoriaID", categoriaId)
            .get()
            .addOnSuccessListener { resultado ->
                productos.clear()
                for (document in resultado) {
                    val producto = Producto(
                        id = document.getString("ID") ?: "",
                        categoriaId = document.getString("CategoriaID") ?: "",
                        descripcion = document.getString("Descripcion") ?: "",
                        imagenUrl = document.getString("ImagenURL") ?: "",
                        nombre = document.getString("Nombre") ?: "",
                        precio = document.getDouble("Precio") ?: 0.0,
                        vendedorId = document.getString("VendedorID") ?: "",
                        vendedorNombre = document.getString("VendedorNombre") ?: ""
                    )
                    productos.add(producto)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
    }

}
