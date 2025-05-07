package com.example.campusgo.ui.compra

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.data.models.Pedido
import com.example.campusgo.databinding.ActivityListaComprasPasadasBinding
import com.example.campusgo.databinding.ActivityListaProductosVendidosBinding
import com.example.campusgo.ui.adapters.PedidoAdapter
import com.example.campusgo.ui.mapas.MapaCompradorActivity
import com.example.campusgo.ui.mapas.MapaVendedorActivity
import com.example.campusgo.ui.producto.DetalleProductoActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ListaComprasPasadasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaComprasPasadasBinding
    private lateinit var adapter: PedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaComprasPasadasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }
        // Set the layout manager here
        binding.productos.layoutManager = LinearLayoutManager(this)
        //Create the adapter
        adapter = PedidoAdapter(mutableListOf()) { pedido ->
            val intent = Intent(this, MapaCompradorActivity::class.java)
            intent.putExtra("compradorID", pedido.vendedorId)
            startActivity(intent)
        }
        //set the adapter to the recycler view
        binding.productos.adapter = adapter

        inicializarLista()

    }

    private fun inicializarLista() {
        val db = FirebaseFirestore.getInstance()
        val usuario = Firebase.auth.currentUser
        db.collection("Pedidos")
            .whereEqualTo("compradorID", usuario?.uid)
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