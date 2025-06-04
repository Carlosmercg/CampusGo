package com.example.campusgo.ui.compra

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.ui.home.HomeActivity
import com.example.campusgo.ui.main.BottomMenuActivity
import com.example.campusgo.ui.mapas.MapaDireccionActivity
import com.example.campusgo.ui.mapas.MapaVendedorActivity
import com.example.campusgo.ui.venta.VentaActivity
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.osmdroid.util.GeoPoint

class ComprarActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityComprarBinding
    private var productosSeleccionados: List<Producto> = emptyList()
    private var direccionSeleccionada: String? = null
    private var productosIdCarrito: List<String> = emptyList()



    private lateinit var mapaLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComprarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Usar Serializable en lugar de Parcelable
        @Suppress("UNCHECKED_CAST")
        productosSeleccionados = intent.getSerializableExtra("productos") as? ArrayList<Producto> ?: emptyList()
        productosIdCarrito = intent.getSerializableExtra("carritos") as? ArrayList<String> ?: emptyList()

        if (productosSeleccionados.isEmpty()) {
            Toast.makeText(this, "No hay productos para comprar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val vendedorId = productosSeleccionados[0].vendedorId

        buscarImagen(vendedorId) { fotoPerfil ->
            ManejadorImagenesAPI.mostrarImagenDesdeUrl(
                fotoPerfil,
                binding.ivVendedorFoto,
                baseContext,
                R.drawable.ic_profile,
            )
        }

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

        mapaLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val direccion = data?.getStringExtra("direccion")
                if (!direccion.isNullOrBlank()) {
                    direccionSeleccionada = direccion
                    binding.etPuntoEntrega.setText(direccion)
                    binding.btnComprar.isEnabled = true
                }
            }
        }

        // Calcular totales
        val subtotal = productosSeleccionados.sumOf { it.precio }
        val envio = 5000.0
        val total = subtotal + envio

        binding.tvSubtotal.text = "Subtotal: $${subtotal}"
        binding.tvEnvio.text = "Envío: $${envio}"
        binding.tvTotal.text = "TOTAL: $${total}"

        binding.ivBack.setOnClickListener{
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        binding.btnComprar.isEnabled = false

        binding.etPuntoEntrega.setOnClickListener {
            val intent = Intent(this, MapaDireccionActivity::class.java)
            mapaLauncher.launch(intent)
        }

        binding.btnComprar.setOnClickListener {

            val direccion = direccionSeleccionada ?: return@setOnClickListener
            val db = FirebaseFirestore.getInstance()
            val uid = Firebase.auth.currentUser?.uid ?: return@setOnClickListener
            val pedidoId = db.collection("Pedidos").document().id
            val fecha = Timestamp.now()

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
                "compradorId" to uid,
                "direccion" to direccion,
                "estado" to estado,
                "fecha" to fecha,
                "metodoPago" to metodoPago,
                "latvendedor" to latVendedor,
                "longvendedor" to longVendedor,
                "vendedorId" to productosSeleccionados[0].vendedorId,
                "productos" to productosMapeados
            )

            db.collection("Pedidos").document(pedidoId)
                .set(pedido)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pedido creado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al crear pedido: ${it.message}", Toast.LENGTH_LONG).show()
                }

            db.collection("usuarios").document(uid)
                .update("compras", FieldValue.arrayUnion(pedidoId))

            db.collection("usuarios").document(productosSeleccionados[0].vendedorId)
                .update("ventas", FieldValue.arrayUnion(pedidoId))

            productosIdCarrito.forEach { carritoId ->
                db.collection("Carrito").document(carritoId)
                    .update("estado", "comprado")
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar carrito $carritoId: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
    private fun buscarImagen(Id: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .document(Id)
            .get()
            .addOnSuccessListener { usuarioDoc ->
                val imagen = usuarioDoc.getString("urlFotoPerfil") ?: "imagen desconocida"
                callback(imagen)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener imagen", e)
                callback("Nombre desconocido")
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
