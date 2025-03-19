package com.example.campusgo.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.models.Producto
import java.net.URL
import java.util.concurrent.Executors

class ProductoAdapter(
    private val productos: List<Producto>,
    private val onItemClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
        holder.itemView.setOnClickListener { onItemClick(productos[position]) }
    }

    override fun getItemCount(): Int = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProducto: ImageView = itemView.findViewById(R.id.imgProducto)
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombreProducto)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecioProducto)

        fun bind(producto: Producto) {
            txtNombre.text = producto.nombre
            txtPrecio.text = "$${producto.precio}"
            loadImageFromUrl(producto.imagenUrl, imgProducto)
        }

        private fun loadImageFromUrl(urlString: String, imageView: ImageView) {
            Executors.newSingleThreadExecutor().execute {
                try {
                    val url = URL(urlString)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(url.openStream())
                    imageView.post { imageView.setImageBitmap(bitmap) }
                } catch (e: Exception) {
                    Log.e("ProductoAdapter", "Error cargando imagen: ${e.message}")
                    imageView.post { imageView.setImageResource(R.drawable.ic_placeholder) }
                }
            }
        }
    }
}
