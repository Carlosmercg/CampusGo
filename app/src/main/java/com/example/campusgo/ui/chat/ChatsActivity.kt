package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.databinding.ActivityChatsBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatsBinding
    private lateinit var adapter: FirebaseRecyclerAdapter<Chat, ChatViewHolder>
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMensajeria)
        supportActionBar?.apply {
            title = getString(R.string.menu_chats)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }

        setupRecycler()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecycler() {
        val uid = auth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("chats")
        val query = ref.orderByChild("idUsuario").equalTo(uid)

        val options = FirebaseRecyclerOptions.Builder<Chat>()
            .setQuery(query, Chat::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Chat, ChatViewHolder>(options) {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ChatViewHolder {
                val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                return ChatViewHolder(view)
            }

            override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Chat) {
                holder.bind(model)
                holder.itemView.setOnClickListener {
                    val intent = Intent(this@ChatsActivity, ChatActivity::class.java).apply {
                        putExtra("chatId", model.id)
                        putExtra("nombreUsuario", model.nombreUsuario)
                    }
                    startActivity(intent)
                }
            }
        }

        binding.recyclerChats.layoutManager = LinearLayoutManager(this)
        binding.recyclerChats.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    class ChatViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun bind(chat: Chat) {
            itemView.findViewById<TextView>(android.R.id.text1).text = chat.nombreUsuario
            itemView.findViewById<TextView>(android.R.id.text2).text = chat.ultimoMensaje
        }
    }
}
