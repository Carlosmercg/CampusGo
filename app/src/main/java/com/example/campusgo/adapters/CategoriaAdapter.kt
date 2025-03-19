package com.example.campusgo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.models.Categoria

class CategoriaAdapter(private val categorias: List<Categoria>, private val onItemClick: (Categoria) -> Unit) :
    RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.bind(categoria)
        holder.itemView.setOnClickListener { onItemClick(categoria) }
    }

    override fun getItemCount(): Int = categorias.size

    class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcono: ImageView = itemView.findViewById(R.id.img_categoria)
        private val txtNombre: TextView = itemView.findViewById(R.id.txt_nombre_categoria)

        fun bind(categoria: Categoria) {
            txtNombre.text = categoria.nombre
            imgIcono.setImageResource(categoria.icono) // Se usa un recurso drawable local
        }
    }
}
