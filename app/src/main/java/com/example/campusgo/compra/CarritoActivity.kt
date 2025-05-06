package com.example.campusgo.compra

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.BottomMenuActivity
import com.example.campusgo.databinding.ActivityCarritoBinding
import com.example.campusgo.databinding.ItemCarritoBinding
import com.example.campusgo.databinding.ItemVendedorBinding
import com.example.campusgo.models.Producto

class CarritoActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private val productosCarrito = mutableListOf<Producto>()
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

        productosCarrito.addAll(
            listOf(
                Producto("P1", "Compás Técnico", "vendedorA", " ",12000.0, "", "Vendedor A", "Descripción"),
                Producto("P2", "Regla T", "vendedorA", " ",8000.0, "", "Vendedor A", "Descripción"),
                Producto("P3", "Atlas Médico", "vendedorB", " ",45000.0, "", "Vendedor B", "Descripción")
            )
        )

        cargarRecycler()
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_carrito)
    }

    private fun cargarRecycler() {
        val items = mutableListOf<Any>()
        productosCarrito.groupBy { it.vendedorNombre }.forEach { (vendedor, productos) ->
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
                else if (holder is ProductoVH) holder.bind(items[position] as Producto)
            }

            inner class VendedorVH(private val vb: ItemVendedorBinding) :
                RecyclerView.ViewHolder(vb.root) {
                fun bind(vendedorNombre: String) {
                    vb.txtVendedor.text = vendedorNombre
                    vb.btnConfirmar.setOnClickListener {
                        val productosDelVendedor = productosCarrito.filter { it.vendedorNombre == vendedorNombre }
                        val intent = Intent(this@CarritoActivity, ComprarActivity::class.java)
                        intent.putExtra("productos", ArrayList(productosDelVendedor))
                        startActivity(intent)
                    }
                }
            }

            inner class ProductoVH(private val pb: ItemCarritoBinding) :
                RecyclerView.ViewHolder(pb.root) {
                fun bind(producto: Producto) {
                    pb.txtNombreProductoCarrito.text = producto.nombre
                    pb.txtPrecioProductoCarrito.text = "$${producto.precio}"

                    Glide.with(pb.imgProductoCarrito.context)
                        .load(producto.imagenUrl.ifEmpty { R.drawable.ic_placeholder })
                        .into(pb.imgProductoCarrito)

                    pb.btnEliminarProducto.setOnClickListener {
                        productosCarrito.remove(producto)
                        cargarRecycler()
                    }
                }
            }
        }

        binding.recyclerCarrito.layoutManager = LinearLayoutManager(this)
        binding.recyclerCarrito.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
    }
}
