package com.example.campusgo.ui.compra

import android.R.attr.title
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.data.models.Producto
import com.example.campusgo.data.models.ProductoCarrito
import com.example.campusgo.databinding.ActivityCarritoBinding
import com.example.campusgo.databinding.ItemCarritoBinding
import com.example.campusgo.databinding.ItemVendedorBinding
import com.example.campusgo.ui.main.BottomMenuActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class CarritoActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private val productosCarrito = mutableListOf<ProductoCarrito>()
    private lateinit var adapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarCarrito)
        supportActionBar?.apply {
            title = getString(R.string.pedidos)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }
        cargarCarritoDesdeFirestore()
        cargarRecycler()
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_carrito)

    }


    private fun cargarRecycler() {
        val items = mutableListOf<Any>()
        productosCarrito.groupBy { it.producto.vendedorNombre }.forEach { (vendedor, productos) ->
            items.add(vendedor)
            items.addAll(productos)
        }

        adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun getItemViewType(position: Int) = if (items[position] is String) 0 else 1
            override fun getItemCount() = items.size

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return if (viewType == 0) {
                    val vb = ItemVendedorBinding.inflate(inflater, parent, false)
                    VendedorVH(vb)
                } else {
                    val pb = ItemCarritoBinding.inflate(inflater, parent, false)
                    ProductoVH(pb)
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                if (holder is VendedorVH) holder.bind(items[position] as String)
                else if (holder is ProductoVH) holder.bind(items[position] as ProductoCarrito)
            }

            inner class VendedorVH(private val vb: ItemVendedorBinding) :
                RecyclerView.ViewHolder(vb.root) {
                fun bind(vendedorNombre: String) {
                    vb.txtVendedor.text = vendedorNombre
                    vb.btnConfirmar.setOnClickListener {
                        val productosDelVendedor = productosCarrito
                            .filter { it.producto.vendedorNombre == vendedorNombre }
                            .map { it.producto }

                        val productosIdCarrito = productosCarrito
                            .filter { it.producto.vendedorNombre == vendedorNombre }
                            .map { it.carritoDocId }

                        val intent = Intent(this@CarritoActivity, ComprarActivity::class.java)
                        intent.putExtra("productos", ArrayList(productosDelVendedor))
                        intent.putExtra("carritos", ArrayList(productosIdCarrito))
                        startActivity(intent)
                    }
                }
            }

            inner class ProductoVH(private val pb: ItemCarritoBinding) :
                RecyclerView.ViewHolder(pb.root) {
                fun bind(producto: ProductoCarrito) {
                    pb.txtNombreProductoCarrito.text = producto.producto.nombre
                    pb.txtPrecioProductoCarrito.text = "$${producto.producto.precio}"

                    Glide.with(pb.imgProductoCarrito.context)
                        .load(producto.producto.imagenUrl.ifEmpty { R.drawable.ic_placeholder })
                        .into(pb.imgProductoCarrito)

                    pb.btnEliminarProducto.setOnClickListener {
                        productosCarrito.remove(producto)
                        cargarRecycler()
                        eliminarProductoDelCarrito(producto)
                    }
                }
            }
        }

        binding.recyclerCarrito.layoutManager = LinearLayoutManager(this)
        binding.recyclerCarrito.adapter = adapter
    }

    private fun cargarCarritoDesdeFirestore() {

        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Carrito")
            .whereEqualTo("compradorId", uid)
            .whereEqualTo("estado", "activo")
            .get()
            .addOnSuccessListener { carritoDocs ->
                if (carritoDocs.isEmpty) {
                    productosCarrito.clear()
                    cargarRecycler()
                    return@addOnSuccessListener
                }

                productosCarrito.clear()

                val totalProductos = carritoDocs.size()
                var productosCargados = 0

                for (doc in carritoDocs) {
                    val productoId = doc.getString("productoId") ?: continue
                    val carritoDocId = doc.id

                    db.collection("Productos").document(productoId)
                        .get()
                        .addOnSuccessListener { productoDoc ->
                            productoDoc?.let {
                                val producto = Producto(
                                    id = it.id,
                                    nombre = it.getString("nombre") ?: "",
                                    vendedorId = it.getString("vendedorId") ?: "",
                                    imagenUrl = it.getString("imagenUrl") ?: "",
                                    precio = it.getDouble("precio") ?: 0.0,
                                    descripcion = it.getString("descripcion") ?: "",
                                    vendedorNombre = it.getString("vendedorNombre") ?: ""
                                )
                                productosCarrito.add(ProductoCarrito(producto, carritoDocId))
                            }
                            productosCargados++
                            if (productosCargados == totalProductos) {
                                cargarRecycler()
                            }
                        }
                        .addOnFailureListener {
                            productosCargados++
                            if (productosCargados == totalProductos) {
                                cargarRecycler()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar carrito", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarProductoDelCarrito(productoCarrito: ProductoCarrito) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Carrito").document(productoCarrito.carritoDocId)
            .delete()
            .addOnSuccessListener {
                productosCarrito.remove(productoCarrito)
                cargarRecycler()
                Toast.makeText(this, "Producto eliminado del carrito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar producto", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
    }
}
