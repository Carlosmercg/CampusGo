package com.example.campusgo.ui.usuario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.example.campusgo.databinding.ActivityPerfilBinding
import com.example.campusgo.ui.compra.ListaComprasPasadasActivity
import com.example.campusgo.ui.home.HomeActivity
import com.example.campusgo.ui.producto.CrearProductoActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usuario = Firebase.auth.currentUser
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios").document(usuario?.uid ?: "").get()
            .addOnSuccessListener { document ->
                val nombre = document.getString("nombre")
                val apellido = document.getString("apellido")
                val fotoPerfil = document.getString("fotoPerfilURL")
                binding.nombres.text = nombre + " " + apellido
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
    }

}