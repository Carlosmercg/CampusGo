package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.databinding.ActivityChatsListBinding
import com.example.campusgo.ui.adapters.ChatListViewAdapter
import com.example.campusgo.ui.adapters.UsuarioAdapter
import com.example.campusgo.ui.main.BottomMenuActivity
import com.example.campusgo.ui.usuario.PerfilActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ChatsListActivity : BottomMenuActivity() {

    private lateinit var binding: ActivityChatsListBinding
    private lateinit var chatListViewAdapter: ChatListViewAdapter
    private lateinit var usuarioAdapter: UsuarioAdapter

    private val listaChats = mutableListOf<Chat>()
    private val resultadosBusqueda = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        setupBottomNavigation(binding.bottomNavigation, R.id.nav_chats)
        setupRecyclerViews()
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
        return item.itemId == android.R.id.home.also { finish() } || super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerViews() {
        chatListViewAdapter = ChatListViewAdapter(listaChats) { chatSeleccionado ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("nombreUsuario", chatSeleccionado.nombreUsuario)
                putExtra("chatId", chatSeleccionado.id)
                putExtra("uidReceptor", chatSeleccionado.uidReceptor)
                putExtra("fotoPerfilUrl", chatSeleccionado.urlFotoPerfil)
            }
            startActivity(intent)
        }

        binding.recyclerChats.layoutManager = LinearLayoutManager(this)
        binding.recyclerChats.adapter = chatListViewAdapter

        usuarioAdapter = UsuarioAdapter(
            usuarios = resultadosBusqueda,
            onChatearClick = { usuario -> iniciarChatCon(usuario) },
            onPerfilClick = { usuario -> irAlPerfil(usuario) }
        )

        binding.recyclerResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerResultados.adapter = usuarioAdapter
    }

    private fun setupBuscador() {
        val filtros = resources.getStringArray(R.array.filtros_busqueda)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.etBuscarUsuario.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText.orEmpty().trim()
                if (texto.length >= 2) buscarUsuariosFirestore(texto)
                else {
                    resultadosBusqueda.clear()
                    usuarioAdapter.notifyDataSetChanged()
                }
                return true
            }
        })
    }

    private fun cargarChatsDesdeFirebase() {
        val uidActual = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("usuariosChats/$uidActual")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                                        listaChats.add(
                                            Chat(
                                                id = chatId,
                                                nombreUsuario = usuario.nombre,
                                                ultimoMensaje = ultimoMensaje,
                                                timestamp = System.currentTimeMillis(),
                                                uidReceptor = uidReceptor,
                                                urlFotoPerfil = usuario.urlFotoPerfil
                                            )
                                        )
                                    }
                                    processedChats++
                                    if (processedChats == totalChats) {
                                        chatListViewAdapter.notifyDataSetChanged()
                                        mostrarSinChats(listaChats.isEmpty())
                                    }
                                }.addOnFailureListener {
                                    processedChats++
                                    if (processedChats == totalChats) {
                                        chatListViewAdapter.notifyDataSetChanged()
                                        mostrarSinChats(listaChats.isEmpty())
                                    }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            processedChats++
                            if (processedChats == totalChats) {
                                chatListViewAdapter.notifyDataSetChanged()
                                mostrarSinChats(listaChats.isEmpty())
                            }
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                mostrarSinChats(true)
            }
        })
    }

    private fun mostrarSinChats(vacio: Boolean) {
        binding.tvSinChats.visibility = if (vacio) View.VISIBLE else View.GONE
        binding.recyclerChats.visibility = if (vacio) View.GONE else View.VISIBLE
    }

    private fun buscarUsuariosFirestore(valor: String) {
        val campo = when (binding.spinnerFiltro.selectedItem.toString().lowercase()) {
            "nombre" -> "nombre"
            "carrera" -> "carrera"
            "universidad" -> "universidad"
            else -> "nombre"
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .whereGreaterThanOrEqualTo(campo, valor)
            .whereLessThanOrEqualTo(campo, valor + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                resultadosBusqueda.clear()
                resultadosBusqueda.addAll(result.toObjects(Usuario::class.java))
                usuarioAdapter.notifyDataSetChanged()
            }
    }

    private fun iniciarChatCon(usuario: Usuario) {
        val uidActual = FirebaseAuth.getInstance().uid ?: return
        val chatId = if (uidActual < usuario.id) "$uidActual-${usuario.id}" else "${usuario.id}-$uidActual"

        FirebaseDatabase.getInstance().getReference("chats/$chatId/participantes")
            .setValue(mapOf(uidActual to true, usuario.id to true))

        FirebaseDatabase.getInstance().getReference("usuariosChats/$uidActual/$chatId")
            .setValue(true)
        FirebaseDatabase.getInstance().getReference("usuariosChats/${usuario.id}/$chatId")
            .setValue(true)

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("uidReceptor", usuario.id)
            putExtra("nombreUsuario", usuario.nombre)
            putExtra("fotoPerfilUrl", usuario.urlFotoPerfil)
        }
        startActivity(intent)
    }

    private fun irAlPerfil(usuario: Usuario) {
        val intent = Intent(this, PerfilActivity::class.java).apply {
            putExtra("uid", usuario.id)
        }
        startActivity(intent)
    }
}
