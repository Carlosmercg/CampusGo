package com.example.campusgo.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.data.models.Pedido
import com.example.campusgo.databinding.ActivityListaProductosVendidosBinding
import com.example.campusgo.ui.adapters.PedidoAdapter
import com.example.campusgo.ui.mapas.MapaVendedorActivity
import com.example.campusgo.ui.producto.DetalleProductoActivity
import com.example.campusgo.ui.venta.VentaActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ListaProductosVendidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaProductosVendidosBinding
    private lateinit var adapter: PedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaProductosVendidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }
        // Set the layout manager here
        binding.productos.layoutManager = LinearLayoutManager(this)
        //Create the adapter
        adapter = PedidoAdapter(mutableListOf()) { pedido ->

            when (pedido.estado.lowercase()) {
                "aceptado" -> {
                    val intent = Intent(this, MapaVendedorActivity::class.java)
                    intent.putExtra("pedidoID", pedido.id)
                    startActivity(intent)
                }

                "activo" -> {
                    val intent = Intent(this, VentaActivity::class.java)
                    intent.putExtra("pedidoID", pedido.id)
                    startActivity(intent)
                }
                "rechazado" -> {
                    Toast.makeText(this, "Este pedido fue rechazado", Toast.LENGTH_SHORT).show()
                }

                "terminado" -> {
                    Toast.makeText(this, "Este pedido ya está terminado", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Estado del pedido desconocido", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //set the adapter to the recycler view
        binding.productos.adapter = adapter

        inicializarLista()

    }

    private fun inicializarLista() {
        val db = FirebaseFirestore.getInstance()
        val usuario = Firebase.auth.currentUser
        db.collection("Pedidos")
            .whereEqualTo("vendedorId", usuario?.uid)
            .get()
            .addOnSuccessListener { result ->
                val pedidos = mutableListOf<Pedido>()
                for (document in result) {
                    pedidos.add(document.toObject(Pedido::class.java))
                }
                // Update the data in the adapter using the correct method
                adapter.updateData(pedidos)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al leer productos", exception)
            }
    }
}