package com.example.campusgo.ui.chat

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.campusgo.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.data.models.Mensaje
import com.example.campusgo.databinding.ActivityChatBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: FirebaseRecyclerAdapter<Mensaje, MensajeViewHolder>
    private var chatId: String? = null
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent.getStringExtra("chatId")
        setupToolbar()
        setupRecycler()
        setupEnviarBtn()
    }

    private fun setupToolbar() {
        val nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        binding.chatToolbar.title = nombreUsuario
        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    private fun setupRecycler() {
        val mensajesRef = FirebaseDatabase.getInstance()
            .getReference("mensajes")
            .child(chatId ?: return)

        val options = FirebaseRecyclerOptions.Builder<Mensaje>()
            .setQuery(mensajesRef, Mensaje::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Mensaje, MensajeViewHolder>(options) {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MensajeViewHolder {
                val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                return MensajeViewHolder(view)
            }

            override fun onBindViewHolder(holder: MensajeViewHolder, position: Int, model: Mensaje) {
                holder.bind(model)
            }
        }

        binding.recyclerMensajes.layoutManager = LinearLayoutManager(this)
        binding.recyclerMensajes.adapter = adapter
    }

    private fun setupEnviarBtn() {
        binding.btnEnviar.setOnClickListener {
            val texto = binding.etMensaje.text.toString().trim()
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            if (!TextUtils.isEmpty(texto) && chatId != null) {
                val mensaje = Mensaje(contenido = texto, emisorId = uid, timestamp = System.currentTimeMillis())
                val ref = FirebaseDatabase.getInstance()
                    .getReference("mensajes")
                    .child(chatId!!)
                    .push()
                ref.setValue(mensaje)
                    .addOnSuccessListener { binding.etMensaje.setText("") }
                    .addOnFailureListener { Log.e("ChatActivity", "Error al enviar mensaje", it) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    class MensajeViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun bind(mensaje: Mensaje) {
            val textView = itemView.findViewById<TextView>(android.R.id.text1)
            textView.text = mensaje.contenido
        }
    }
}


