package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.databinding.ActivityChatsListBinding
import com.example.campusgo.ui.adapters.ChatListViewAdapter
import com.example.campusgo.ui.main.BottomMenuActivity

class ChatsListActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityChatsListBinding
    private lateinit var chatListViewAdapter: ChatListViewAdapter
    private val listaChats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_chats)
        setupRecyclerView()

        // 🔄 Reemplaza esto luego con los datos reales desde Firebase
        cargarDatosDePrueba()
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbarMensajeria)
        supportActionBar?.apply {
            title = getString(R.string.menu_chats)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }
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
        chatListViewAdapter = ChatListViewAdapter(listaChats) { chatSeleccionado ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("nombreUsuario", chatSeleccionado.nombreUsuario)
                putExtra("chatId", chatSeleccionado.id)
            }
            startActivity(intent)
        }

        binding.recyclerChats.apply {
            layoutManager = LinearLayoutManager(this@ChatsListActivity)
            adapter = chatListViewAdapter
            setHasFixedSize(true)
        }
    }

    private fun cargarDatosDePrueba() {
        listaChats.clear()
        listaChats.addAll(
            listOf(
                Chat("1", "Juan Pérez", "Hola, ¿tienes el libro disponible?", 1710853400),
                Chat("2", "María López", "Gracias, ya realicé el pago.", 1710853460)
            )
        )
        chatListViewAdapter.notifyDataSetChanged()
    }
}
