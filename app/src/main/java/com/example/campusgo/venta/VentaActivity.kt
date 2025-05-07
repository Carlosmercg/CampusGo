package com.example.campusgo.compra

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.adapters.ProductoAdapter
import com.example.campusgo.databinding.ActivityVentaBinding
import com.example.campusgo.models.Producto
import com.example.campusgo.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class VentaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVentaBinding
    private lateinit var productoAdapter: ProductoAdapter
    // Lista mutable para que el adapter pueda actualizarse
    private val productosVenta = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.ivBack.setOnClickListener { onBackPressed() }


        val pedidoId = intent.getStringExtra("pedidoId") ?: run {
            Toast.makeText(this, "Pedido inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        productoAdapter = ProductoAdapter(productosVenta) { /* click opcional */ }
        binding.rvProductosVenta.apply {
            layoutManager = LinearLayoutManager(this@VentaActivity)
            adapter = productoAdapter
        }

        // Cargamos todos los datos del pedido
        FirebaseFirestore.getInstance()
            .collection("Pedidos")
            .document(pedidoId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 1) Dirección
                binding.tvDireccion.text = doc.getString("direccion") ?: "-"

                // 2) Hora (campo fecha → Timestamp)
                doc.getTimestamp("fecha")?.toDate()?.let { date ->
                    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    binding.tvHora.text = fmt.format(date)
                }

                // 3) Méto-do de pago
                val metodo = doc.getString("metodoPago") ?: ""
                binding.tvMetodoText.text = metodo.uppercase(Locale.getDefault())
                val iconRes = if (metodo.equals("tarjeta", true))
                    R.drawable.credit_card
                else
                    R.drawable.cash
                binding.imgIconPago.setImageResource(iconRes)

                // 4) Lista de productos embebidos en el pedido
                @Suppress("UNCHECKED_CAST")
                val listaMap = doc.get("productos") as? List<Map<String, Any>> ?: emptyList()
                val listaProductos = listaMap.map { m ->
                    // parseo defensivo de precio
                    val rawPrecio = m["precio"]
                    val precio = when (rawPrecio) {
                        is Number -> rawPrecio.toDouble()
                        is String -> rawPrecio.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    Producto(
                        id             = m["id"]                as? String ?: "",
                        nombre         = m["nombre"]            as? String ?: "",
                        vendedorId     = m["vendedorId"]        as? String ?: "",
                        categoriaId    = m["categoriaId"]       as? String ?: "",
                        precio         = precio,
                        imagenUrl      = m["imagenUrl"]         as? String ?: "",
                        vendedorNombre = m["vendedorNombre"]    as? String ?: "",
                        descripcion    = m["descripcion"]       as? String ?: ""
                    )
                }
                // Actualizamos el adapter
                productoAdapter.updateList(listaProductos)

                // 5) Cargar datos del comprador
                val compradorId = doc.getString("compradorID") ?: ""
                if (compradorId.isNotBlank()) cargarComprador(compradorId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando pedido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarComprador(uid: String) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val usr = doc.toObject(Usuario::class.java)
                if (usr != null) {
                    // Nombre completo
                    binding.tvCompradorNombre.text = "${usr.nombre} ${usr.apellido}"
                    // Foto de perfil
                    if (usr.fotoPerfilUrl.isNotBlank()) {
                        Glide.with(this)
                            .load(usr.fotoPerfilUrl)
                            .placeholder(R.drawable.placeholder_usuario)
                            .error(R.drawable.placeholder_usuario)
                            .into(binding.imgComprador)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando comprador", Toast.LENGTH_SHORT).show()
            }
    }
}
