package com.example.campusgo.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.data.repository.DescargadorImagenes
import com.example.campusgo.data.session.UsuarioSesion
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

        val usuario = UsuarioSesion.usuarioActual
        if (usuario == null) {
            Toast.makeText(this, "Sesión expirada. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Configurar Toolbar
        setSupportActionBar(binding.toolbarMensajeria)
        supportActionBar?.apply {
            title = getString(R.string.menu_chats)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }

        setupRecycler(usuario.id)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecycler(uid: String) {
        val ref = FirebaseDatabase.getInstance().getReference("chats")

        val query = ref.orderByChild("usuarios/$uid").equalTo(true)

        val options = FirebaseRecyclerOptions.Builder<Chat>()
            .setQuery(query, Chat::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Chat, ChatViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat, parent, false)
                return ChatViewHolder(view)
            }

            override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Chat) {
                holder.bind(model)
                holder.itemView.setOnClickListener {
                    val intent = Intent(this@ChatsActivity, ChatActivity::class.java).apply {
                        putExtra("chatId", model.id)
                        putExtra("nombreUsuario", model.nombreUsuario)
                        putExtra("fotoUrl", model.fotoUrl)
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

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val txtNombre = itemView.findViewById<TextView>(R.id.txtNombreUsuario)
        private val txtMensaje = itemView.findViewById<TextView>(R.id.txtUltimoMensaje)

        fun bind(chat: Chat) {
            txtNombre.text = chat.nombreUsuario
            txtMensaje.text = chat.ultimoMensaje

            val descargador = DescargadorImagenes(itemView.context)
            if (!chat.fotoUrl.isNullOrBlank()) {
                descargador.cargarEn(imageView, chat.fotoUrl, R.drawable.placeholder_usuario)
            } else {
                imageView.setImageResource(R.drawable.placeholder_usuario)
            }
        }
    }
}
