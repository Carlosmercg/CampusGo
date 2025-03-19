package com.example.campusgo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusgo.databinding.ActivityDetalleProductoBinding

class DetalleProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleProductoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombre = intent.getStringExtra("nombreProducto") ?: "Producto"
        val precio = intent.getDoubleExtra("precioProducto", 0.0)
        val descripcion = intent.getStringExtra("descripcionProducto") ?: "Sin descripción"
        val imagenUrl = intent.getStringExtra("imagenProducto")

        binding.txtTituloProducto.text = nombre
        binding.txtPrecio.text = "$$precio"
        binding.txtDescripcionProducto.text = descripcion

        Glide.with(this).load(imagenUrl).into(binding.imgProducto)
    }
}
