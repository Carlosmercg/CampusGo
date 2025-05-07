package com.example.campusgo.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.ui.adapters.MensajeAdapter
import com.example.campusgo.databinding.ActivityChatBinding
import com.example.campusgo.data.models.Mensaje

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var mensajeAdapter: MensajeAdapter
    private val mensajes = mutableListOf<Mensaje>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadDummyMessages()
    }

    private fun setupToolbar() {
        val nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        supportActionBar?.title = nombreUsuario
    }

    private fun setupRecyclerView() {
        mensajeAdapter = MensajeAdapter(mensajes)
        binding.recyclerMensajes.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = mensajeAdapter
        }
    }

    private fun loadDummyMessages() {
        mensajes.apply {
            add(Mensaje("Hola, ¿sigues vendiendo el producto?", true))
            add(Mensaje("Sí, aún está disponible", false))
        }
        mensajeAdapter.notifyDataSetChanged()
    }
}
