package com.example.campusgo.ui.compra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityListaComprasPasadasBinding
import com.example.campusgo.databinding.ItemCarritoBinding
import com.example.campusgo.data.models.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListaComprasPasadasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaComprasPasadasBinding
    private val productosComprados = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaComprasPasadasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerCarrito.layoutManager = LinearLayoutManager(this)
        binding.recyclerCarrito.adapter = ComprasAdapter()

        cargarComprasDelUsuario()
    }

    private fun cargarComprasDelUsuario() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val comprasIds = doc.get("compras") as? List<String> ?: emptyList()
                if (comprasIds.isEmpty()) return@addOnSuccessListener

                db.collection("productos")
                    .whereIn("id", comprasIds)
                    .get()
                    .addOnSuccessListener { result ->
                        productosComprados.clear()
                        for (productoDoc in result) {
                            val producto = productoDoc.toObject(Producto::class.java)
                            productosComprados.add(producto)
                        }
                        binding.recyclerCarrito.adapter?.notifyDataSetChanged()
                    }
            }
    }

    inner class ComprasAdapter : RecyclerView.Adapter<ComprasAdapter.ProductoViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemCarritoBinding.inflate(inflater, parent, false)
            return ProductoViewHolder(itemBinding)
        }

        override fun getItemCount(): Int = productosComprados.size

        override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
            holder.bind(productosComprados[position])
        }

        inner class ProductoViewHolder(private val binding: ItemCarritoBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(producto: Producto) {
                binding.txtNombreProductoCarrito.text = producto.nombre
                binding.txtPrecioProductoCarrito.text = "$${producto.precio}"

                Glide.with(binding.imgProductoCarrito.context)
                    .load(producto.imagenUrl.ifEmpty { R.drawable.ic_placeholder })
                    .into(binding.imgProductoCarrito)

                binding.btnEliminarProducto.visibility = android.view.View.GONE
            }
        }
    }
}
