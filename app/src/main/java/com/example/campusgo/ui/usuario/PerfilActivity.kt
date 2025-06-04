package com.example.campusgo.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.campusgo.R

import com.example.campusgo.databinding.ActivityPerfilBinding
import com.example.campusgo.ui.auth.LoginActivity
import com.example.campusgo.ui.compra.ListaComprasPasadasActivity
import com.example.campusgo.ui.home.HomeActivity
import com.example.campusgo.ui.main.BottomMenuActivity
import com.example.campusgo.ui.producto.CrearProductoActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.campusgo.data.repository.ManejadorImagenesAPI


class PerfilActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private var fotoPerfil: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation(binding.bottomNavigation, R.id.nav_cuenta)
        val usuario = Firebase.auth.currentUser
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(usuario?.uid ?: "").get()
            .addOnSuccessListener { document ->
                val nombre = document.getString("nombre")
                val apellido = document.getString("apellido")
                fotoPerfil = document.getString("urlFotoPerfil")
                binding.nombres.text = nombre + " " + apellido
                ManejadorImagenesAPI.mostrarImagenDesdeUrl(
                    fotoPerfil,
                    binding.profilePicture,
                    this,
                    R.drawable.ic_profile,
                )
            }
            .addOnFailureListener { exception ->
                Log.e("PerfilActivity", "Error al obtener los datos del usuario", exception)
            }


        binding.back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.editarPerfil.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }

        binding.pedidos.setOnClickListener {
            val intent = Intent(this, ListaProductosVendidosActivity::class.java)
            startActivity(intent)
        }

        binding.subirProducto.setOnClickListener {
            val intent = Intent(this, CrearProductoActivity::class.java)
            startActivity(intent)
        }

        binding.comprasMasRecientes.setOnClickListener {
            val intent = Intent(this, ListaComprasPasadasActivity::class.java)
            startActivity(intent)
        }

        binding.cerrarSesion.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        ManejadorImagenesAPI.mostrarImagenDesdeUrl(
            fotoPerfil,
            binding.profilePicture,
            this,
            R.drawable.ic_profile,
        )
    }

}