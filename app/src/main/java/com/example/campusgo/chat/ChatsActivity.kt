package com.example.campusgo.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.adapters.ChatAdapter
import com.example.campusgo.databinding.ActivityChatsBinding
import com.example.campusgo.models.Chat

class ChatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatsBinding
    private lateinit var chatAdapter: ChatAdapter
    private val listaChats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("ChatsActivityTest", "onCreate ChatsActivity iniciado")

        // 1) Configurar la Toolbar como ActionBar
        setSupportActionBar(binding.toolbarMensajeria)
        supportActionBar?.apply {
            title = getString(R.string.menu_chats)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }

        // 2) Configurar RecyclerView
        setupRecyclerView()

        // 3) Cargar datos (simulados)
        loadDummyData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(listaChats) { chatSeleccionado ->
            Intent(this, ChatActivity::class.java).apply {
                putExtra("nombreUsuario", chatSeleccionado.nombreUsuario)
                putExtra("chatId", chatSeleccionado.id)
            }.also { startActivity(it) }
        }

        binding.recyclerChats.apply {
            layoutManager = LinearLayoutManager(this@ChatsActivity)
            adapter = chatAdapter
        }
    }

    private fun loadDummyData() {
        listaChats.clear()
        listaChats.addAll(ChatManager.obtenerChats())
        chatAdapter.notifyDataSetChanged()
    }
    override fun onResume() {
        super.onResume()
        loadDummyData() // Actualizar lista al regresar
    }
}
