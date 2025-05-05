package com.example.campusgo.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.adapters.MensajeAdapter
import com.example.campusgo.databinding.ActivityChatBinding
import com.example.campusgo.models.Mensaje

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var mensajeAdapter: MensajeAdapter
    private val mensajes = mutableListOf<Mensaje>()
    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chatId = intent.getStringExtra("chatId")
        setupEnviarBtn()
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
    private fun setupEnviarBtn() {
        binding.btnEnviar.setOnClickListener {
            val mensajeTexto = binding.etMensaje.text.toString().trim()
            if (mensajeTexto.isNotEmpty()) {
                // Crear y añadir mensaje
                val nuevoMensaje = Mensaje(mensajeTexto, true)
                mensajes.add(nuevoMensaje)
                mensajeAdapter.notifyItemInserted(mensajes.size - 1)
                binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)

                // Actualizar último mensaje en Chats
                actualizarUltimoMensajeEnChats(mensajeTexto)

                binding.etMensaje.setText("")
            }
        }
    }
    private fun actualizarUltimoMensajeEnChats(mensaje: String) {
        chatId?.let { id ->
            ChatManager.actualizarChat(
                id,
                nuevoMensaje = mensaje,
                nuevoTimestamp = System.currentTimeMillis()
            )
        }
    }
}
