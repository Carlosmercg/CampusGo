package com.example.campusgo.producto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.adapters.ProductoAdapter
import com.example.campusgo.databinding.ActivityListaProductosBinding
import com.example.campusgo.models.Producto
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

        cargarProductosDesdeFirebase(categoriaId)
    }

    private fun cargarProductosDesdeFirebase(categoriaId: String) {
        FirebaseFirestore.getInstance()
            .collection("productos")
            .whereEqualTo("categoriaId", categoriaId)
            .get()
            .addOnSuccessListener { resultado ->
                productos.clear()
                for (document in resultado) {
                    val producto = document.toObject(Producto::class.java).copy(id = document.id)
                    productos.add(producto)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
    }
}
