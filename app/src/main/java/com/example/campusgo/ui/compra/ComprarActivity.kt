package com.example.campusgo.ui.compra

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.data.models.Producto
import com.example.campusgo.databinding.ActivityComprarBinding
import com.example.campusgo.databinding.ItemCarritoBinding
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.ui.venta.VentaActivity
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
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
            val db = FirebaseFirestore.getInstance()
            val uid = Firebase.auth.currentUser?.uid ?: return@setOnClickListener
            val pedidoId = db.collection("Pedidos").document().id
            val fecha = Timestamp.now()

            val direccion = binding.etPuntoEntrega.text.toString()
            val estado = "activo"
            lateinit var metodoPago: String
            if(binding.spinnerMetodoPago.selectedItemId.toString()=="1"){
                metodoPago = "Efectivo"
            } else metodoPago = "Tarjeta"


            val latVendedor = 4.7338184
            val longVendedor = -74.0381694

            val productosMapeados = productosSeleccionados.map { producto ->
                mapOf(
                    "id" to producto.id,
                    "nombre" to producto.nombre,
                    "descripcion" to producto.descripcion,
                    "imagenUrl" to producto.imagenUrl,
                    "precio" to producto.precio,
                    "categoriaId" to producto.categoriaId,
                    "vendedorId" to producto.vendedorId,
                    "vendedorNombre" to producto.vendedorNombre
                )
            }

            val pedido = mapOf(
                "id" to pedidoId,
                "compradorID" to uid,
                "direccion" to direccion,
                "estado" to estado,
                "fecha" to fecha,
                "metodoPago" to metodoPago,
                "latvendedor" to latVendedor,
                "longvendedor" to longVendedor,
                "vendedorID" to productosSeleccionados[0].vendedorId,
                "productos" to productosMapeados
            )

            db.collection("Pedidos").document(pedidoId)
                .set(pedido)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pedido creado correctamente", Toast.LENGTH_SHORT).show()
                    // Ir a siguiente pantalla si quieres
                    startActivity(Intent(this, VentaActivity::class.java).putExtra("pedidoId", pedidoId))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al crear pedido: ${it.message}", Toast.LENGTH_LONG).show()
                }

            db.collection("usuarios").document(uid)
                .update("compras", FieldValue.arrayUnion(pedidoId))

            db.collection("usuarios").document(productosSeleccionados[0].vendedorId)
                .update("ventas", FieldValue.arrayUnion(pedidoId))

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
