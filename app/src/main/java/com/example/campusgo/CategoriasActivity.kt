package com.example.campusgo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campusgo.adapters.CategoriaAdapter
import com.example.campusgo.databinding.ActivityCategoriasBinding
import com.example.campusgo.models.Categoria

class CategoriasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriasBinding
    private lateinit var categoriaAdapter: CategoriaAdapter
    private val categorias = listOf(
        Categoria("1", "Mecánica", R.drawable.ic_mecanica),
        Categoria("2", "Arquitectura", R.drawable.ic_arquitectura),
        Categoria("3", "Artes Visuales", R.drawable.ic_artes),
        Categoria("4", "Música", R.drawable.ic_musica),
        Categoria("5", "Medicina", R.drawable.ic_medicina),
        Categoria("6", "Educación", R.drawable.ic_educacion)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoriaAdapter = CategoriaAdapter(categorias) { categoria ->
            // Implementar navegación a productos de la categoría
        }

        binding.recyclerCategorias.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerCategorias.adapter = categoriaAdapter
    }
}
