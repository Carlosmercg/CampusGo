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
import com.example.campusgo.models.Categoria
import java.util.concurrent.Executors

class CategoriaAdapter(
    private val categorias: List<Categoria>,
    private val onItemClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.bind(categoria, onItemClick)
    }

    override fun getItemCount(): Int = categorias.size

    class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCategoria: ImageView = itemView.findViewById(R.id.img_categoria)
        private val txtNombre: TextView = itemView.findViewById(R.id.txt_nombre_categoria)

        fun bind(categoria: Categoria, onItemClick: (Categoria) -> Unit) {
            txtNombre.text = categoria.nombre
            imgCategoria.setImageResource(categoria.icono)
            itemView.setOnClickListener { onItemClick(categoria) }
        }
    }
}
