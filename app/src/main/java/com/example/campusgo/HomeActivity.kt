package com.example.campusgo.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campusgo.R
import com.example.campusgo.adapters.CategoriaAdapter
import com.example.campusgo.databinding.ActivityHomeBinding
import com.example.campusgo.models.Categoria

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var categoriaAdapter: CategoriaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categorias = listOf(
            Categoria("1", "Mecánica", R.drawable.ic_mecanica),
            Categoria("2", "Arquitectura", R.drawable.ic_arquitectura),
            Categoria("3", "Artes Visuales", R.drawable.ic_artes),
            Categoria("4", "Música", R.drawable.ic_musica),
            Categoria("5", "Medicina", R.drawable.ic_medicina),
            Categoria("6", "Educación", R.drawable.ic_educacion)
        )

        categoriaAdapter = CategoriaAdapter(categorias) { categoria ->
            val intent = Intent(this, CategoriasActivity::class.java)
            intent.putExtra("categoriaId", categoria.id)
            intent.putExtra("categoriaNombre", categoria.nombre)
            startActivity(intent)
        }

        binding.recyclerCategorias.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerCategorias.adapter = categoriaAdapter
    }
}
