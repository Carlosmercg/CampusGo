package com.example.campusgo.Ingresar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.BottomMenuActivity
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityHomeBinding
import com.example.campusgo.databinding.ItemCategoriaBinding
import com.example.campusgo.models.Categoria
import com.example.campusgo.producto.ListaProductosActivity
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val categorias = mutableListOf<Categoria>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        val adapter = CategoriaAdapter()
        binding.recyclerCategorias.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerCategorias.adapter = adapter

        // Cargar categorías desde Firestore
        FirebaseFirestore.getInstance().collection("Categorías")
            .get()
            .addOnSuccessListener { result ->
                categorias.clear()
                for (doc in result) {
                    val categoria = Categoria(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        iconResName = doc.getString("iconResName") ?: ""
                    )
                    categorias.add(categoria)
                }
                adapter.notifyDataSetChanged()
            }

        // Menú inferior
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_home)
    }

    inner class CategoriaAdapter : RecyclerView.Adapter<CategoriaVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaVH {
            val itemBinding = ItemCategoriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CategoriaVH(itemBinding)
        }

        override fun getItemCount(): Int = categorias.size

        override fun onBindViewHolder(holder: CategoriaVH, position: Int) {
            holder.bind(categorias[position])
        }
    }

    inner class CategoriaVH(private val itemBinding: ItemCategoriaBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(categoria: Categoria) {
            itemBinding.txtNombreCategoria.text = categoria.nombre
            val resId = resources.getIdentifier(categoria.iconResName, "drawable", packageName)
            itemBinding.imgCategoria.setImageResource(resId)
            itemBinding.root.setOnClickListener {
                val intent = Intent(this@HomeActivity, ListaProductosActivity::class.java)
                intent.putExtra("categoriaNombre", categoria.nombre)
                startActivity(intent)
            }
        }
    }
}
