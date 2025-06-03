package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var adapter: RecyclerView.Adapter<*>

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
        adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount() = listaChats.size

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.item_chat, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val chat = listaChats[position]
                val view = holder.itemView
                view.findViewById<TextView>(R.id.txtNombreUsuario).text = "${chat.nombreUsuario} ${chat.apellidoUsuario}"
                view.findViewById<TextView>(R.id.txtUltimoMensaje).text = chat.ultimoMensaje

                ManejadorImagenesAPI.mostrarImagenDesdeUrl(
                    chat.urlFotoPerfil,
                    view.findViewById(R.id.imgFotoPerfil),
                    this@ChatsListActivity,
                    R.drawable.ic_profile,
                    R.drawable.ic_profile
                )

                view.setOnClickListener {
                    val intent = Intent(this@ChatsListActivity, ChatActivity::class.java).apply {
                        putExtra("chatId", chat.id)
                        putExtra("uidReceptor", chat.uidReceptor)
                        putExtra("nombreUsuario", "${chat.nombreUsuario} ${chat.apellidoUsuario}")
                        putExtra("fotoPerfilUrl", chat.urlFotoPerfil)
                    }
                    startActivity(intent)
                }
            }
        }
        binding.recyclerChats.adapter = adapter
    }

    private fun aplicarFiltro(query: String?) {
        val texto = query?.trim()?.lowercase() ?: ""
        val filtrados = if (texto.isEmpty()) {
            listaChats
        } else {
            when (filtroActual) {
                "apellido" -> listaChats.filter { it.apellidoUsuario.lowercase().contains(texto) }
                else -> listaChats.filter { it.nombreUsuario.lowercase().contains(texto) }
            }
        }

        mostrarSinChats(filtrados.isEmpty())
        binding.recyclerChats.scrollToPosition(0)
        listaChats.clear()
        listaChats.addAll(filtrados)
        adapter.notifyDataSetChanged()
    }

    private fun cargarChatsDesdeFirestore() {
        val uidActual = FirebaseAuth.getInstance().uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uidActual).collection("listaChats").get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) return@addOnSuccessListener mostrarSinChats(true)

                var processed = 0
                val total = result.size()

                result.forEach { doc ->
                    val uidReceptor = doc.id
                    val chatId = doc.getString("chatId") ?: return@forEach

                    db.collection("usuarios").document(uidReceptor).get().addOnSuccessListener { usuarioDoc ->
                        val usuario = usuarioDoc.toObject(Usuario::class.java) ?: return@addOnSuccessListener
                        val realtimeRef = FirebaseDatabase.getInstance().getReference("chats/$chatId/ultimoMensaje")

                        realtimeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val ultimoMensaje = snapshot.getValue(String::class.java) ?: ""
                                listaChats.add(
                                    Chat(
                                        id = chatId,
                                        uidReceptor = uidReceptor,
                                        nombreUsuario = usuario.nombre,
                                        apellidoUsuario = usuario.apellido,
                                        urlFotoPerfil = usuario.urlFotoPerfil,
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
                }
            }
            .addOnFailureListener {
                mostrarSinChats(true)
            }
    }

    private fun mostrarSinChats(vacio: Boolean) {
        binding.tvSinChats.visibility = if (vacio) View.VISIBLE else View.GONE
        binding.scrollChats.visibility = if (vacio) View.GONE else View.VISIBLE
    }
}
