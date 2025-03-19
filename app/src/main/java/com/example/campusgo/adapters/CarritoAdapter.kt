package com.example.campusgo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.models.Producto

class CarritoAdapter(
    private val productos: List<Producto>,
    private val onRemoveClick: (Producto) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        holder.bind(productos[position], onRemoveClick)
    }

    override fun getItemCount(): Int = productos.size

    class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProducto: ImageView = itemView.findViewById(R.id.img_producto_carrito)
        private val txtNombre: TextView = itemView.findViewById(R.id.txt_nombre_producto_carrito)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txt_precio_producto_carrito)
        private val btnEliminar: Button = itemView.findViewById(R.id.btn_eliminar_producto)

        fun bind(producto: Producto, onRemoveClick: (Producto) -> Unit) {
            txtNombre.text = producto.nombre
            txtPrecio.text = "$${producto.precio}"
            btnEliminar.setOnClickListener { onRemoveClick(producto) }

            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(producto.imagenUrl) // Se usa imagenUrl en vez de imagenResId
                .placeholder(R.drawable.ic_placeholder) // Imagen por defecto mientras carga
                .error(R.drawable.ic_error) // Imagen en caso de error
                .into(imgProducto)
        }
    }
}
