package com.example.campusgo.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.ui.main.BottomMenuActivity
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityHomeBinding
import com.example.campusgo.databinding.ItemCategoriaBinding
import com.example.campusgo.data.models.Categoria
import com.example.campusgo.ui.producto.ListaProductosActivity
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val categorias = mutableListOf<Categoria>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = CategoriaAdapter()
        binding.recyclerCategorias.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerCategorias.adapter = adapter

        cargarCategorias(adapter)
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_home)
    }

    private fun cargarCategorias(adapter: CategoriaAdapter) {
        // Asegúrate de usar el mismo nombre que en Firestore: "Categorias" sin tilde
        FirebaseFirestore.getInstance().collection("Categorías")
            .get()
            .addOnSuccessListener { result ->
                categorias.clear()
                for (doc in result) {
                    val nombre = doc.getString("nombre") ?: ""
                    val icono = doc.getString("iconResName") ?: ""

                    if (nombre.isNotBlank() && icono.isNotBlank()) {
                        categorias.add(
                            Categoria(
                                id = doc.id,
                                nombre = nombre,
                                iconResName = icono
                            )
                        )
                    } else {
                        Log.w("HomeActivity", "Documento con campos incompletos: ${doc.id}")
                    }
                }
                Log.i("HomeActivity", "Total categorías cargadas: ${categorias.size}")
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar categorías", Toast.LENGTH_SHORT).show()
                Log.e("HomeActivity", "Fallo al obtener categorías: ${e.message}", e)
            }
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
            val resId = resources.getIdentifier(categoria.iconResName, "drawable", packageName)
            if (resId != 0) {
                itemBinding.imgCategoria.setImageResource(resId)
            } else {
                Log.w("HomeActivity", "Icono no encontrado: ${categoria.iconResName}")
                Toast.makeText(itemView.context, "Icono '${categoria.iconResName}' no encontrado", Toast.LENGTH_SHORT).show()
            }

            itemBinding.root.setOnClickListener {
                val intent = Intent(this@HomeActivity, ListaProductosActivity::class.java)
                intent.putExtra("categoriaNombre", categoria.nombre)
                intent.putExtra("categoriaId", categoria.id)
                startActivity(intent)
            }
        }
    }
}
