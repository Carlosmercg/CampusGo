package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.databinding.ActivityChatsBinding
import com.example.campusgo.ui.adapters.ChatAdapter
import com.example.campusgo.ui.main.BottomMenuActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.recyclerview.widget.LinearLayoutManager

class ChatsActivity : BottomMenuActivity() {
    private lateinit var binding: ActivityChatsBinding
    private lateinit var chatAdapter: ChatAdapter
    private val listaChats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar
        setSupportActionBar(binding.toolbarMensajeria)
        supportActionBar?.apply {
            title = getString(R.string.menu_chats)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }

        // Configurar BottomNavigation
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_chats)

        // Configurar RecyclerView
        setupRecyclerView()

        // Datos de prueba
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
        listaChats.apply {
            add(Chat("1", "Juan Pérez", "Hola, ¿tienes el libro disponible?", 1710853400))
            add(Chat("2", "María López", "Gracias, ya realicé el pago.", 1710853460))
        }
        chatAdapter.notifyDataSetChanged()
    }
}