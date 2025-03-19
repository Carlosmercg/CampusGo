package com.example.campusgo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cuenta.setOnClickListener {
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

        binding.mediosDePago.setOnClickListener {
            val intent = Intent(this, PerfilMetodosDePagoActivity::class.java)
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