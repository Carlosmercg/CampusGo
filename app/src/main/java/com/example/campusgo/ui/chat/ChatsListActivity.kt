package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.databinding.ActivityChatsListBinding
import com.example.campusgo.ui.main.BottomMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class ChatsListActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityChatsListBinding
    private val listaChats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_chats)
        setupBuscador()
        cargarChatsDesdeFirestore()

        binding.NewChat.setOnClickListener {
            startActivity(Intent(this, BuscarUsuariosActivity::class.java))
        }
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
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupBuscador() {
        val filtros = arrayOf("nombre", "apellido", "carrera", "universidad")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.etBuscarUsuario.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                aplicarFiltro(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                aplicarFiltro(newText)
                return true
            }
        })
    }

    private fun aplicarFiltro(query: String?) {
        val texto = query?.trim()?.lowercase() ?: ""
        binding.recyclerChats.removeAllViews()

        val filtrados = if (texto.isEmpty()) {
            listaChats
        } else {
            listaChats.filter { it.nombreUsuario.lowercase().contains(texto) }
        }

        if (filtrados.isEmpty()) {
            mostrarSinChats(true)
        } else {
            mostrarSinChats(false)
            filtrados.forEach { agregarChatUI(it) }
        }
    }

    private fun cargarChatsDesdeFirestore() {
        val uidActual = FirebaseAuth.getInstance().uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .document(uidActual)
            .collection("listaChats")
            .get()
            .addOnSuccessListener { result ->
                binding.recyclerChats.removeAllViews()
                listaChats.clear()

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
                            val usuario = usuarioDoc.toObject(Usuario::class.java)
                            if (usuario != null) {
                                val realtimeRef = FirebaseDatabase.getInstance()
                                    .getReference("chats/$chatId/ultimoMensaje")

                                realtimeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val ultimoMensaje = snapshot.getValue(String::class.java) ?: ""

                                        val chat = Chat(
                                            id = chatId,
                                            nombreUsuario = usuario.nombre,
                                            uidReceptor = uidReceptor,
                                            urlFotoPerfil = usuario.urlFotoPerfil,
                                            ultimoMensaje = ultimoMensaje,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        listaChats.add(chat)

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
                            } else {
                                processed++
                                if (processed == total) {
                                    aplicarFiltro(binding.etBuscarUsuario.query.toString())
                                }
                            }
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

    private fun agregarChatUI(chat: Chat) {
        val itemView = layoutInflater.inflate(R.layout.item_chat, binding.recyclerChats, false)
        itemView.findViewById<TextView>(R.id.txtNombreUsuario).text = chat.nombreUsuario
        itemView.findViewById<TextView>(R.id.txtUltimoMensaje).text = chat.ultimoMensaje

        ManejadorImagenesAPI.mostrarImagenDesdeUrl(
            chat.urlFotoPerfil,
            itemView.findViewById(R.id.imgFotoPerfil),
            this,
            R.drawable.ic_profile,
            R.drawable.ic_profile
        )

        itemView.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("chatId", chat.id)
                putExtra("uidReceptor", chat.uidReceptor)
                putExtra("nombreUsuario", chat.nombreUsuario)
                putExtra("fotoPerfilUrl", chat.urlFotoPerfil)
            }
            startActivity(intent)
        }

        binding.recyclerChats.addView(itemView)
    }

    private fun mostrarSinChats(vacio: Boolean) {
        binding.tvSinChats.visibility = if (vacio) View.VISIBLE else View.GONE
        binding.recyclerChats.visibility = if (vacio) View.GONE else View.VISIBLE
    }
}
