package com.example.campusgo.usuario

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.Ingresar.HomeActivity
import com.example.campusgo.compra.ListaComprasPasadasActivity
import com.example.campusgo.compra.SubirProductoActivity
import com.example.campusgo.databinding.ActivityPerfilBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val usuario = Firebase.auth.currentUser
        /*usuario?.let {
            val nombre = it.displayName
            binding.nombres.text = nombre
        }*/

        binding.back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.editarPerfil.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }

        binding.pedidos.setOnClickListener {
            val intent = Intent(this, ListaComprasPasadasActivity::class.java)
            startActivity(intent)
        }

        binding.subirProducto.setOnClickListener {
            val intent = Intent(this, SubirProductoActivity::class.java)
            startActivity(intent)
        }

        binding.comprasMasRecientes.setOnClickListener {
            val intent = Intent(this, ListaComprasPasadasActivity::class.java)
            startActivity(intent)
        }
    }
}