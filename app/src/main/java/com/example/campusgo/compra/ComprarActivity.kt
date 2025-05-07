package com.example.campusgo.compra

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityComprarBinding
import com.example.campusgo.databinding.ItemCarritoBinding
import com.example.campusgo.models.Producto
import com.example.campusgo.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ComprarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComprarBinding
    private var productosSeleccionados: List<Producto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComprarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Usar Serializable en lugar de Parcelable
        @Suppress("UNCHECKED_CAST")
        productosSeleccionados = intent.getSerializableExtra("productos") as? ArrayList<Producto> ?: emptyList()

        if (productosSeleccionados.isEmpty()) {
            Toast.makeText(this, "No hay productos para comprar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val vendedorId = productosSeleccionados[0].vendedorId
        obtenerDatosVendedor(vendedorId)

        // Configurar RecyclerView
        binding.recyclerProductos.layoutManager = LinearLayoutManager(this)
        binding.recyclerProductos.adapter = object : RecyclerView.Adapter<ProductoVH>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ProductoVH {
                val itemBinding = ItemCarritoBinding.inflate(layoutInflater, parent, false)
                return ProductoVH(itemBinding)
            }

            override fun onBindViewHolder(holder: ProductoVH, position: Int) {
                holder.bind(productosSeleccionados[position])
            }

            override fun getItemCount(): Int = productosSeleccionados.size
        }

        // Calcular totales
        val subtotal = productosSeleccionados.sumOf { it.precio }
        val envio = 5000.0
        val total = subtotal + envio

        binding.tvSubtotal.text = "Subtotal: $${subtotal}"
        binding.tvEnvio.text = "Envío: $${envio}"
        binding.tvTotal.text = "TOTAL: $${total}"

        binding.btnComprar.setOnClickListener {
            Toast.makeText(this, "Compra confirmada para vendedor", Toast.LENGTH_LONG).show()
            obtenerUltimoPedidoId { pedidoId ->
                // Lanzamos VentaActivity con el ID
                Intent(this, VentaActivity::class.java).apply {
                    putExtra("pedidoId", pedidoId)
                }.also { startActivity(it) }
            }
            finish()
        }
    }
    private fun obtenerUltimoPedidoId(callback: (String) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("Pedidos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshots ->
                val doc = snapshots.documents.firstOrNull()
                val id = doc?.getString("id") ?: doc?.id
                if (!id.isNullOrBlank()) {
                    callback(id)
                } else {
                    Toast.makeText(this, "No se encontró ningún pedido", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener pedido: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun obtenerDatosVendedor(vendedorId: String) {
        FirebaseFirestore.getInstance().collection("usuarios")
            .document(vendedorId)
            .get()
            .addOnSuccessListener { documento ->
                val vendedor = documento.toObject(Usuario::class.java)
                if (vendedor != null) {
                    binding.tvVendedorName.text = "${vendedor.nombre} ${vendedor.apellido}"
                } else {
                    binding.tvVendedorName.text = "Desconocido"
                }
            }
            .addOnFailureListener {
                binding.tvVendedorName.text = "Error"
                Toast.makeText(this, "No se pudo cargar el vendedor", Toast.LENGTH_SHORT).show()
            }
    }

    inner class ProductoVH(private val item: ItemCarritoBinding) : RecyclerView.ViewHolder(item.root) {
        fun bind(producto: Producto) {
            item.txtNombreProductoCarrito.text = producto.nombre
            item.txtPrecioProductoCarrito.text = "$${producto.precio}"

            Glide.with(item.imgProductoCarrito.context)
                .load(producto.imagenUrl.ifEmpty { R.drawable.ic_placeholder })
                .into(item.imgProductoCarrito)

            item.btnEliminarProducto.visibility = View.GONE
        }
    }
}
