package com.example.campusgo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.campusgo.adapters.CategoriaAdapter
import com.example.campusgo.databinding.ActivityHomeBinding
import com.example.campusgo.models.Categoria
import com.example.campusgo.ui.CategoriasActivity

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
            val intent = Intent(this,   CategoriasActivity::class.java)
            intent.putExtra("categoriaId", categoria.id)
            intent.putExtra("categoriaNombre", categoria.nombre)
            startActivity(intent)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_cart -> {
                    val intent = Intent(this, CarritoActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_account -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        binding.recyclerCategorias.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerCategorias.adapter = categoriaAdapter
    }
}
