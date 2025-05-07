package com.example.campusgo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusgo.databinding.ItemProductoBinding
import com.example.campusgo.data.models.Producto

class ProductoAdapter(
    private val productos: List<Producto>,
    private val onItemClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size

    inner class ProductoViewHolder(private val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: Producto) {
            binding.txtNombreProducto.text = producto.nombre
            binding.txtPrecioProducto.text = "$${producto.precio}"

            // Cargar imagen desde URL remota
            Glide.with(binding.root.context)
                .load(producto.imagenUrl)
                .into(binding.imgProducto)

            // Evento de clic
            binding.root.setOnClickListener { onItemClick(producto) }
        }
    }

    fun updateList(nuevos: List<Producto>) {
        if (productos is MutableList) {
            (productos as MutableList<Producto>).apply {
                clear()
                addAll(nuevos)
            }
            notifyDataSetChanged()
        } else {
            throw IllegalStateException("Para usar updateList() debes pasar un MutableList al constructor")
        }
    }
}
