package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.databinding.ActivityChatsListBinding
import com.example.campusgo.ui.main.BottomMenuActivity
import com.example.campusgo.ui.usuario.PerfilActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

class ChatsListActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityChatsListBinding
    private val resultadosBusqueda = mutableListOf<Usuario>()
    private val listaChats = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_chats)
        setupBuscador()
        cargarChatsDesdeFirebase()

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
        val filtros = resources.getStringArray(R.array.filtros_busqueda)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.etBuscarUsuario.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText.orEmpty().trim()
                filtrarChatsPorNombre(texto)
                return true
            }
        })
    }

    private fun filtrarChatsPorNombre(filtro: String) {
        binding.recyclerChats.removeAllViews()
        if (filtro.isEmpty()) {
            listaChats.forEach { agregarChatUI(it) }
        } else {
            val filtrados = listaChats.filter { it.nombreUsuario.contains(filtro, ignoreCase = true) }
            filtrados.forEach { agregarChatUI(it) }
        }
        mostrarSinChats(binding.recyclerChats.childCount == 0)
    }

    private fun cargarChatsDesdeFirebase() {
        val uidActual = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("usuariosChats/$uidActual")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.recyclerChats.removeAllViews()
                listaChats.clear()

                if (!snapshot.exists()) {
                    mostrarSinChats(true)
                    return
                }

                val chatsRef = FirebaseDatabase.getInstance().getReference("chats")
                val totalChats = snapshot.children.count()
                var processedChats = 0

                snapshot.children.forEach { chatSnap ->
                    val chatId = chatSnap.key ?: return@forEach
                    chatsRef.child(chatId).child("ultimoMensaje").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(msnap: DataSnapshot) {
                            val ultimoMensaje = msnap.getValue(String::class.java) ?: ""
                            val partes = chatId.split("-")
                            val uidReceptor = if (partes[0] == uidActual) partes[1] else partes[0]

                            FirebaseFirestore.getInstance().collection("usuarios").document(uidReceptor)
                                .get().addOnSuccessListener { doc ->
                                    val usuario = doc.toObject(Usuario::class.java)
                                    if (usuario != null) {
                                        val chat = Chat(
                                            id = chatId,
                                            nombreUsuario = usuario.nombre,
                                            ultimoMensaje = ultimoMensaje,
                                            timestamp = System.currentTimeMillis(),
                                            uidReceptor = uidReceptor,
                                            urlFotoPerfil = usuario.urlFotoPerfil
                                        )
                                        listaChats.add(chat)
                                    }
                                    processedChats++
                                    if (processedChats == totalChats) filtrarChatsPorNombre(binding.etBuscarUsuario.query.toString())
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                mostrarSinChats(true)
            }
        })
    }

    private fun agregarChatUI(chat: Chat) {
        val itemView = layoutInflater.inflate(R.layout.item_chat, binding.recyclerChats, false)
        itemView.findViewById<TextView>(R.id.txtNombreUsuario).text = chat.nombreUsuario
        itemView.findViewById<TextView>(R.id.txtUltimoMensaje).text = chat.ultimoMensaje

        ManejadorImagenesAPI.mostrarImagenDesdeUrl(
            url = chat.urlFotoPerfil,
            imageView = itemView.findViewById(R.id.imgFotoPerfil),
            context = this,
            placeholderRes = R.drawable.ic_profile,
            errorRes = R.drawable.ic_profile
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
