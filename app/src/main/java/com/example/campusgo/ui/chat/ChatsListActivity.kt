package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.databinding.ActivityChatsListBinding
import com.example.campusgo.databinding.ItemChatBinding
import com.example.campusgo.ui.main.BottomMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class ChatsListActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityChatsListBinding
    private val todosLosChats = mutableListOf<Chat>()
    private val chatsFiltrados = mutableListOf<Chat>()
    private lateinit var adapter: ChatAdapter
    private var filtroActual = "nombre"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_chats)
        setupBuscador()
        setupRecyclerView()
        cargarChatsDesdeFirestore()

        binding.NewChat.setOnClickListener {
            startActivity(Intent(this, BuscarUsuariosActivity::class.java))
        }
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbarMensajeria)
        supportActionBar?.apply {
            title = getString(R.string.menu_chats)
        }

        // Ajusta el padding para evitar que la Toolbar se superponga con la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarMensajeria) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupBuscador() {
        val filtros = arrayOf("nombre", "apellido")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filtroActual = filtros[position]
                aplicarFiltro(binding.etBuscarUsuario.query.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.etBuscarUsuario.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { aplicarFiltro(query) }
            override fun onQueryTextChange(newText: String?) = true.also { aplicarFiltro(newText) }
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerChats.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter()
        binding.recyclerChats.adapter = adapter
    }

    private fun aplicarFiltro(query: String?) {
        val texto = query?.trim()?.lowercase() ?: ""
        chatsFiltrados.clear()

        if (texto.isEmpty()) {
            chatsFiltrados.addAll(todosLosChats)
        } else {
            chatsFiltrados.addAll(todosLosChats.filter {
                when (filtroActual) {
                    "apellido" -> it.apellidoUsuario.lowercase().contains(texto)
                    else -> it.nombreUsuario.lowercase().contains(texto)
                }
            })
        }

        mostrarSinChats(chatsFiltrados.isEmpty())
        adapter.notifyDataSetChanged()
    }

    private fun cargarChatsDesdeFirestore() {
        val uidActual = FirebaseAuth.getInstance().uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uidActual).collection("listaChats").get()
            .addOnSuccessListener { result ->
                todosLosChats.clear()

                if (result.isEmpty) {
                    mostrarSinChats(true)
                    return@addOnSuccessListener
                }

                var processed = 0
                val total = result.size()

                result.forEach { doc ->
                    val uidReceptor = doc.id
                    val chatId = doc.getString("chatId") ?: return@forEach

                    db.collection("usuarios").document(uidReceptor).get()
                        .addOnSuccessListener { usuarioDoc ->
                            val nombre = usuarioDoc.getString("nombre") ?: ""
                            val apellido = usuarioDoc.getString("apellido") ?: ""
                            val urlFoto = usuarioDoc.getString("urlFotoPerfil") ?: ""

                            val realtimeRef = FirebaseDatabase.getInstance()
                                .getReference("chats/$chatId/ultimoMensaje")

                            realtimeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val ultimoMensaje = snapshot.getValue(String::class.java) ?: ""

                                    todosLosChats.add(
                                        Chat(
                                            id = chatId,
                                            uidReceptor = uidReceptor,
                                            nombreUsuario = nombre,
                                            apellidoUsuario = apellido,
                                            urlFotoPerfil = urlFoto,
                                            ultimoMensaje = ultimoMensaje,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )

                                    processed++
                                    if (processed == total) {
                                        aplicarFiltro(binding.etBuscarUsuario.query.toString())
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    processed++
                                    if (processed == total) {
                                        aplicarFiltro(binding.etBuscarUsuario.query.toString())
                                    }
                                }
                            })
                        }
                        .addOnFailureListener {
                            processed++
                            if (processed == total) {
                                aplicarFiltro(binding.etBuscarUsuario.query.toString())
                            }
                        }
                }
            }
            .addOnFailureListener {
                mostrarSinChats(true)
            }
    }

    private fun mostrarSinChats(vacio: Boolean) {
        binding.tvSinChats.visibility = if (vacio) View.VISIBLE else View.GONE
        binding.recyclerChats.visibility = if (vacio) View.GONE else View.VISIBLE
    }

    private inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        inner class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ChatViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chatsFiltrados[position]
            holder.binding.txtNombreUsuario.text = "${chat.nombreUsuario} ${chat.apellidoUsuario}"
            holder.binding.txtUltimoMensaje.text = chat.ultimoMensaje

            Glide.with(this@ChatsListActivity)
                .load(chat.urlFotoPerfil)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(holder.binding.imgPerfilUsuario)

            holder.binding.root.setOnClickListener {
                val intent = Intent(this@ChatsListActivity, ChatActivity::class.java).apply {
                    putExtra("chatId", chat.id)
                    putExtra("uidReceptor", chat.uidReceptor)
                    putExtra("nombreUsuario", chat.nombreUsuario)
                    putExtra("urlFotoPerfil", chat.urlFotoPerfil)
                }
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int = chatsFiltrados.size
    }
}
