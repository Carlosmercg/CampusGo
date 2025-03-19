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

class ProductoAdapter(private val productos: List<Producto>, private val onItemClick: (Producto) -> Unit) :
    RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.bind(producto)
        holder.itemView.setOnClickListener { onItemClick(producto) }
    }

    override fun getItemCount(): Int = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProducto: ImageView = itemView.findViewById(R.id.img_producto)
        private val txtNombre: TextView = itemView.findViewById(R.id.txt_nombre_producto)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txt_precio_producto)

        fun bind(producto: Producto) {
            txtNombre.text = producto.nombre
            txtPrecio.text = "$${producto.precio}"
            loadImageFromUrl(producto.imagenUrl, imgProducto)
        }
    }
}
