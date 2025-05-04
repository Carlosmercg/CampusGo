package com.example.campusgo.Ingresar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.BottomMenuActivity
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityHomeBinding
import com.example.campusgo.databinding.ItemCategoriaBinding
import com.example.campusgo.producto.ListaProductosActivity
import com.example.campusgo.usuario.PerfilActivity
import com.example.campusgo.compra.CarritoActivity
import com.example.campusgo.chat.ChatsActivity

class HomeActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val nombres by lazy {
        resources.getStringArray(R.array.categorias)
    }

    private val iconos = listOf(
        R.drawable.ic_mecanica,
        R.drawable.ic_arquitectura,
        R.drawable.ic_artes,
        R.drawable.ic_musica,
        R.drawable.ic_medicina,
        R.drawable.ic_educacion
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Grid de categorías
        binding.recyclerCategorias.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerCategorias.adapter = object : RecyclerView.Adapter<CategoriaVH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaVH {
                val itemBinding = ItemCategoriaBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                return CategoriaVH(itemBinding)
            }

            override fun getItemCount(): Int = nombres.size

            override fun onBindViewHolder(holder: CategoriaVH, position: Int) {
                holder.bind(nombres[position], iconos[position])
            }
        }

        // Bottom Navigation
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_home)
    }

    inner class CategoriaVH(private val itemBinding: ItemCategoriaBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(nombre: String, iconRes: Int) {
            itemBinding.txtNombreCategoria.text = nombre
            itemBinding.imgCategoria.setImageResource(iconRes)
            itemBinding.root.setOnClickListener {
                val intent = Intent(this@HomeActivity, ListaProductosActivity::class.java)
                intent.putExtra("categoriaNombre", nombre)
                startActivity(intent)
            }
        }
    }
}
