package com.example.campusgo.compra
//Carrito→ Productos agregados con opción de pago.
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.adapters.CarritoAdapter
import com.example.campusgo.databinding.ActivityCarritoBinding
import com.example.campusgo.models.Producto

class CarritoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarritoBinding
    private lateinit var carritoAdapter: CarritoAdapter
    private val productosCarrito = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnComprar.setOnClickListener {
            val intent = Intent(this, ComprarActivity::class.java)
            startActivity(intent)
        }

        carritoAdapter = CarritoAdapter(productosCarrito) { producto ->
            productosCarrito.remove(producto)
            carritoAdapter.notifyDataSetChanged()
        }

        binding.recyclerCarrito.layoutManager = LinearLayoutManager(this)
        binding.recyclerCarrito.adapter = carritoAdapter
    }
}
