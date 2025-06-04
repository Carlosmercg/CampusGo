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
            .whereEqualTo("categoriaId", categoriaId)
            .get()
            .addOnSuccessListener { resultado ->
                productos.clear()
                for (document in resultado) {
                    val producto = Producto(
                        id = document.getString("id") ?: "",
                        categoriaId = document.getString("categoriaId") ?: "",
                        descripcion = document.getString("descripcion") ?: "",
                        imagenUrl = document.getString("imagenUrl") ?: "",
                        nombre = document.getString("nombre") ?: "",
                        precio = document.getDouble("precio") ?: 0.0,
                        vendedorId = document.getString("vendedorId") ?: "",
                        vendedorNombre = document.getString("vendedorNombre") ?: ""
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
