package com.example.campusgo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.databinding.ItemCategoriaBinding
import com.example.campusgo.models.Categoria

class CategoriaAdapter(
    private val categorias: List<Categoria>,
    private val onItemClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val binding = ItemCategoriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.bind(categorias[position])
    }

    override fun getItemCount(): Int = categorias.size

    inner class CategoriaViewHolder(private val binding: ItemCategoriaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoria: Categoria) {
            binding.txtNombreCategoria.text = categoria.nombre
            binding.imgCategoria.setImageResource(categoria.imagenResId)

            // Evento de clic
            binding.root.setOnClickListener { onItemClick(categoria) }
        }
    }
}
