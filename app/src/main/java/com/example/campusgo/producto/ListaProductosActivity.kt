package com.example.campusgo.producto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.BottomMenuActivity
import com.example.campusgo.R
import com.example.campusgo.adapters.ProductoAdapter
import com.example.campusgo.databinding.ActivityListaProductosBinding
import com.example.campusgo.models.Producto
import com.google.firebase.firestore.FirebaseFirestore

class ListaProductosActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityListaProductosBinding
    private lateinit var adapter: ProductoAdapter
    private val productos = mutableListOf<Producto>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaProductosBinding.inflate(layoutInflater)
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_home)
        setContentView(binding.root)

        val categoriaId     = intent.getStringExtra("categoriaId")     ?: ""
        val categoriaNombre = intent.getStringExtra("categoriaNombre") ?: "Productos"


        binding.toolbarLista.title = categoriaNombre
        setSupportActionBar(binding.toolbarLista)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarLista.setNavigationOnClickListener { finish() }


        adapter = ProductoAdapter(productos) { producto ->
            val intent = Intent(this, DetalleProductoActivity::class.java).apply {
                putExtra("productoId",          producto.id)
                putExtra("productoNombre",      producto.nombre)
                putExtra("productoDescripcion", producto.descripcion)
                putExtra("productoPrecio",      producto.precio)
                putExtra("productoImagenUrl",   producto.imagenUrl)
                putExtra("vendedorId",          producto.vendedorId)
                putExtra("vendedorNombre",      producto.vendedorNombre)
            }
            startActivity(intent)
        }


        binding.rvProductos.layoutManager = LinearLayoutManager(this)
        binding.rvProductos.adapter = adapter

        cargarProductos(categoriaId)
    }

    private fun cargarProductos(categoriaId: String) {
        db.collection("Productos")
            .whereEqualTo("CategoriaID", categoriaId)
            .get()
            .addOnSuccessListener { resultado ->
                productos.clear()
                for (doc in resultado) {
                    val prod = Producto(
                        id             = doc.id,
                        nombre         = doc.getString("Nombre")         ?: "",
                        descripcion    = doc.getString("Descripcion")    ?: "",
                        precio         = doc.getDouble("Precio")         ?: 0.0,
                        imagenUrl      = doc.getString("ImagenURL")      ?: "",
                        vendedorId     = doc.getString("VendedorID")     ?: "",
                        vendedorNombre = doc.getString("VendedorNombre") ?: ""
                    )
                    productos.add(prod)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando productos", Toast.LENGTH_SHORT).show()
            }
    }

}
