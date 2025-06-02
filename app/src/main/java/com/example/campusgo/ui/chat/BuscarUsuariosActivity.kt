package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.databinding.ActivityBuscarUsuariosBinding
import com.example.campusgo.ui.adapters.UsuarioAdapter
import com.example.campusgo.ui.usuario.PerfilActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class BuscarUsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuscarUsuariosBinding
    private lateinit var usuarioAdapter: UsuarioAdapter
    private val resultadosBusqueda = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        setupBuscador()
        setupRecycler()
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbarBuscar)
        supportActionBar?.apply {
            title = "Buscar usuarios"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }

        binding.toolbarBuscar.setNavigationOnClickListener {
            val intent = Intent(this, ChatsListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupRecycler() {
        usuarioAdapter = UsuarioAdapter(
            usuarios = resultadosBusqueda,
            onChatearClick = { usuario -> iniciarChatCon(usuario) },
            onPerfilClick = { usuario -> irAlPerfil(usuario) }
        )
        binding.recyclerResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerResultados.adapter = usuarioAdapter
    }

    private fun setupBuscador() {
        val filtros = arrayOf("nombre", "apellido", "carrera", "universidad")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.btnBuscar.setOnClickListener {
            val texto = binding.etBuscarUsuario.text.toString().trim()
            val filtro = binding.spinnerFiltro.selectedItem.toString().lowercase()

            if (texto.length >= 2) {
                buscarUsuariosFirestore(filtro, texto)
            } else {
                resultadosBusqueda.clear()
                usuarioAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Escribe al menos 2 caracteres", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarUsuariosFirestore(filtro: String, valor: String) {
        val campo = when (filtro) {
            "nombre" -> "nombre"
            "apellido" -> "apellido"
            "carrera" -> "carrera"
            "universidad" -> "universidad"
            else -> "nombre"
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .orderBy(campo)
            .startAt(valor)
            .endAt(valor + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                resultadosBusqueda.clear()
                resultadosBusqueda.addAll(result.toObjects(Usuario::class.java))
                usuarioAdapter.notifyDataSetChanged()

                if (resultadosBusqueda.isEmpty()) {
                    Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar en Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun iniciarChatCon(usuario: Usuario) {
        val uidActual = FirebaseAuth.getInstance().uid ?: return
        val chatId = if (uidActual < usuario.id) "$uidActual-${usuario.id}" else "${usuario.id}-$uidActual"

        FirebaseDatabase.getInstance().getReference("chats/$chatId/participantes")
            .setValue(mapOf(uidActual to true, usuario.id to true))

        FirebaseDatabase.getInstance().getReference("usuariosChats/$uidActual/$chatId").setValue(true)
        FirebaseDatabase.getInstance().getReference("usuariosChats/${usuario.id}/$chatId").setValue(true)

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("uidReceptor", usuario.id)
            putExtra("nombreUsuario", usuario.nombre)
            putExtra("fotoPerfilUrl", usuario.urlFotoPerfil)
        }
        startActivity(intent)
    }

    private fun irAlPerfil(usuario: Usuario) {
        val intent = Intent(this, PerfilActivity::class.java).apply {
            putExtra("uid", usuario.id)
        }
        startActivity(intent)
    }
}
