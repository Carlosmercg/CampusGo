package com.example.campusgo.compra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.adapters.CarritoAdapter
import com.example.campusgo.databinding.ActivityListaComprasPasadasBinding
import com.example.campusgo.models.Producto

class ListaComprasPasadasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaComprasPasadasBinding
    private lateinit var carritoAdapter: CarritoAdapter
    private val productosCarrito = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaComprasPasadasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carritoAdapter = CarritoAdapter(productosCarrito) { producto ->
            productosCarrito.remove(producto)
            carritoAdapter.notifyDataSetChanged()
        }



        binding.recyclerCarrito.layoutManager = LinearLayoutManager(this)
        binding.recyclerCarrito.adapter = carritoAdapter
    }
}