package com.example.campusgo.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.adapters.ProductoAdapter
import com.example.campusgo.databinding.ActivityCategoriasBinding
import com.example.campusgo.models.Producto

class CategoriasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriasBinding
    private lateinit var productoAdapter: ProductoAdapter
    private var productos = listOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoriaId = intent.getStringExtra("categoriaId") ?: "0"
        val categoriaNombre = intent.getStringExtra("categoriaNombre") ?: "Categoría"

        binding.txtCategoriaTitulo.text = categoriaNombre

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        productos = obtenerProductosPorCategoria(categoriaId)

        productoAdapter = ProductoAdapter(productos) { producto ->
            val intent = Intent(this, DetalleProductoActivity::class.java)
            intent.putExtra("nombreProducto", producto.nombre)
            intent.putExtra("precioProducto", producto.precio)
            intent.putExtra("descripcionProducto", producto.descripcion)
            intent.putExtra("imagenProducto", producto.imagenUrl)
            startActivity(intent)
        }

        binding.recyclerProductos.layoutManager = LinearLayoutManager(this)
        binding.recyclerProductos.adapter = productoAdapter
    }

    private fun obtenerProductosPorCategoria(categoriaId: String): List<Producto> {
        return when (categoriaId) {
            "1" -> listOf(
                Producto("1", "Llave Inglesa", "Herramienta para mecánica", 150.0, "https://example.com/llave.jpg"),
                Producto("2", "Tornillos de Precisión", "Tornillos de alta calidad", 50.0, "https://example.com/tornillos.jpg")
            )
            "2" -> listOf(
                Producto("3", "Regla para Arquitectura", "Regla con medidas exactas", 100.0, "https://example.com/regla.jpg"),
                Producto("4", "Escalímetro", "Instrumento de medición", 120.0, "https://example.com/escalimetro.jpg")
            )
            else -> listOf(
                Producto("0", "Producto Genérico", "Descripción de producto", 50.0, "https://example.com/default.jpg")
            )
        }
    }
}
