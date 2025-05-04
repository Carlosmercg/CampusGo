package com.example.campusgo.usuario

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusgo.Ingresar.HomeActivity
import com.example.campusgo.R
import com.example.campusgo.compra.ListaComprasPasadasActivity
import com.example.campusgo.databinding.ActivityPerfilBinding
import com.example.campusgo.models.Usuario
import com.example.campusgo.producto.CrearProductoActivity
import com.example.campusgo.usuario.ListaProductosVendidosActivity

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var usuarioActual: Usuario // Esto se obtendrá luego desde Firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Simulación de datos del usuario (luego se cargará desde Firebase)
        usuarioActual = Usuario(
            id = "u1",
            nombre = "Antonio",
            apellido = "Sánchez Montoya",
            universidad = "PUJ",
            correo = "antonio@campusgo.com",
            fotoPerfilUrl = "", // Si estuviera vacío, usa placeholder
        )

        mostrarDatosUsuario()

        // Navegar al Home
        binding.back.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // Editar perfil
        binding.editarPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        // Subir nuevo producto
        binding.subirProducto.setOnClickListener {
            startActivity(Intent(this, CrearProductoActivity::class.java))
        }

        // Ver productos vendidos
        binding.verProductosVendidos.setOnClickListener {
            startActivity(Intent(this, ListaProductosVendidosActivity::class.java))
        }

        // Ver compras pasadas
        binding.comprasMasRecientes.setOnClickListener {
            startActivity(Intent(this, ListaComprasPasadasActivity::class.java))
        }
    }

    private fun mostrarDatosUsuario() {
        // Nombre completo
        val nombreCompleto = "${usuarioActual.nombre} ${usuarioActual.apellido}"
        binding.nombres.text = nombreCompleto

        // Imagen de perfil
        Glide.with(this)
            .load(usuarioActual.fotoPerfilUrl.ifEmpty { R.drawable.ic_placeholder })
            .into(binding.profilePicture)
    }
}
