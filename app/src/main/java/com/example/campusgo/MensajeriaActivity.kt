package com.example.campusgo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.adapters.ChatAdapter
import com.example.campusgo.databinding.ActivityMensajeriaBinding
import com.example.campusgo.models.Chat

class MensajeriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMensajeriaBinding
    private lateinit var chatAdapter: ChatAdapter
    private val listaChats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMensajeriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadDummyData()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(listaChats) { chatSeleccionado ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("nombreUsuario", chatSeleccionado.nombreUsuario)
            }
            startActivity(intent)
        }

        binding.recyclerChats.apply {
            layoutManager = LinearLayoutManager(this@MensajeriaActivity)
            adapter = chatAdapter
        }
    }

    private fun loadDummyData() {
        listaChats.add(Chat("1", "Juan Pérez", "Hola, ¿tienes el libro disponible?", 1710853400))
        listaChats.add(Chat("2", "María López", "Gracias, ya realicé el pago.", 1710853460))
        chatAdapter.notifyDataSetChanged()
    }
}
