package com.example.campusgo.venta

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.adapters.ProductoAdapter
import com.example.campusgo.databinding.ActivityVentaBinding
import com.example.campusgo.data.models.Producto
import com.example.campusgo.data.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class VentaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVentaBinding
    private lateinit var productoAdapter: ProductoAdapter
    private val productosVenta = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Flecha atrás
        binding.ivBack.setOnClickListener { onBackPressed() }

        // ID del pedido (o uno de prueba si no llega)
        val pedidoId = intent.getStringExtra("pedidoId")
            ?: "fcFwgPfYyu9O6gAzftPD"

        // RecyclerView
        productoAdapter = ProductoAdapter(productosVenta) { /* click opcional */ }
        binding.rvProductosVenta.apply {
            layoutManager = LinearLayoutManager(this@VentaActivity)
            adapter = productoAdapter
        }

        // Carga de datos desde Firestore
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

                // 2) Hora
                doc.getTimestamp("fecha")?.toDate()?.let { date ->
                    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    binding.tvHora.text = fmt.format(date)
                }

                // 3) Método de pago
                val metodo = doc.getString("metodoPago") ?: ""
                binding.tvMetodoText.text = metodo.uppercase(Locale.getDefault())
                val iconRes = if (metodo.equals("tarjeta", true))
                    R.drawable.credit_card
                else
                    R.drawable.cash
                binding.imgIconPago.setImageResource(iconRes)

                // 4) Productos embebidos
                @Suppress("UNCHECKED_CAST")
                val listaMap = doc.get("productos") as? List<Map<String, Any>> ?: emptyList()
                val listaProductos = listaMap.map { m ->
                    val rawPrecio = m["precio"]
                    val precio = when (rawPrecio) {
                        is Number -> rawPrecio.toDouble()
                        is String -> rawPrecio.toDoubleOrNull() ?: 0.0
                        else       -> 0.0
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

                // Actualizar lista y adapter
                productosVenta.clear()
                productosVenta.addAll(listaProductos)
                productoAdapter.notifyDataSetChanged()

                // ─── Cálculo de tarifas ────────────────────────────────────────
                val subtotal = listaProductos.sumOf { it.precio }
                val servicio = subtotal * 0.02
                val total    = subtotal + servicio

                // Formateador con dos decimales y separador de miles
                val df = DecimalFormat("#,##0.00")

                binding.tvServicios.text = "$${df.format(servicio)}"
                binding.tvTotal.text     = "Total: $${df.format(total)}"

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
                    binding.tvCompradorNombre.text = "${usr.nombre} ${usr.apellido}"
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
