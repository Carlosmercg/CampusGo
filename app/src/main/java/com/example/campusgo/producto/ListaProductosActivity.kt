package com.example.campusgo.producto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.adapters.ProductoAdapter
import com.example.campusgo.databinding.ActivityListaProductosBinding
import com.example.campusgo.models.Producto
import com.example.campusgo.producto.DetalleProductoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.lifecycleScope
import kotlinx.coroutines.withContext

class ListaProductosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListaProductosBinding
    private lateinit var adapter: ProductoAdapter
    private val productos = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Recuperar categoría de la pantalla anterior
        val categoriaId   = intent.getStringExtra("categoriaId") ?: ""
        val categoriaNombre = intent.getStringExtra("categoriaNombre")
            ?: getString(R.string.home_title)
        // Fijar título dinámicamente
        binding.toolbarLista.title = categoriaNombre
        setSupportActionBar(binding.toolbarLista)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarLista.setNavigationOnClickListener { finish() }

        // 2) Configurar RecyclerView
        adapter = ProductoAdapter(productos) { producto ->
            // Al hacer click vamos a DetalleProductoActivity
            Intent(this, DetalleProductoActivity::class.java).apply {
                putExtra("productoId", producto.id)
            }.also(::startActivity)
        }
        binding.rvProductos.layoutManager = LinearLayoutManager(this)
        binding.rvProductos.adapter = adapter

        // 3) Cargar desde base de datos (Room, Realm, Firebase, _tu_ repositorio...)
        lifecycleScope.launchWhenCreated {
            val lista = withContext(Dispatchers.IO) {
                // Aquí usas tu DAO, repo o servicio. Ejemplo Room:
                AppDatabase.getInstance(this@ListaProductosActivity)
                    .productoDao()
                    .getByCategoria(categoriaId)
            }
            productos.clear()
            productos.addAll(lista)
            adapter.notifyDataSetChanged()
        }
    }
}
