package com.example.campusgo
//Categorías → Lista de categorías de productos.
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.adapters.CategoriaAdapter
import com.example.campusgo.databinding.ActivityCategoriasBinding
import com.example.campusgo.models.Categoria

class CategoriasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriasBinding
    private lateinit var categoriaAdapter: CategoriaAdapter
    private val categorias = listOf(
        Categoria("1", "Libros", R.drawable.ic_books),
        Categoria("2", "Apuntes", R.drawable.ic_notes)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoriaAdapter = CategoriaAdapter(categorias) { categoria ->
            // Implementar navegación a productos de la categoría
        }

        binding.recyclerCategorias.layoutManager = LinearLayoutManager(this)
        binding.recyclerCategorias.adapter = categoriaAdapter
    }
}
