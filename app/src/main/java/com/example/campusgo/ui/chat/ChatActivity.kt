package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.data.models.Mensaje
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.databinding.ActivityChatBinding
import com.example.campusgo.ui.adapters.MensajeAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var mensajeAdapter: MensajeAdapter
    private val mensajes = mutableListOf<Mensaje>()

    private lateinit var dbRef: DatabaseReference
    private lateinit var chatId: String
    private lateinit var uidActual: String
    private lateinit var uidReceptor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uidActual = FirebaseAuth.getInstance().uid ?: return
        chatId = intent.getStringExtra("chatId") ?: return
        uidReceptor = intent.getStringExtra("uidReceptor") ?: return

        configurarToolbar()
        setupRecyclerView()
        escucharMensajes()
        configurarBotonEnviar()
    }

    private fun configurarToolbar() {
        val nombre = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        val fotoPerfilUrl = intent.getStringExtra("fotoPerfilUrl")

        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.nombreUsuarioToolbar.text = nombre

        // ✅ Volver a la lista de chats
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ChatsListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // ✅ Cargar foto desde el Manejador de Imágenes
        ManejadorImagenesAPI.mostrarImagenDesdeUrl(
            url = fotoPerfilUrl,
            imageView = binding.imgPerfilToolbar,
            context = this,
            placeholderRes = R.drawable.ic_profile,
            errorRes = R.drawable.ic_profile
        )
    }

    private fun setupRecyclerView() {
        mensajeAdapter = MensajeAdapter(mensajes)
        binding.recyclerMensajes.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = mensajeAdapter
        }
    }

    private fun configurarBotonEnviar() {
        binding.btnEnviar.setOnClickListener {
            val texto = binding.etMensaje.text.toString().trim()
            if (texto.isNotEmpty()) {
                val mensaje = Mensaje(
                    contenido = texto,
                    emisor = uidActual,
                    receptor = uidReceptor,
                    timestamp = System.currentTimeMillis()
                )

                FirebaseDatabase.getInstance()
                    .getReference("chats/$chatId/messages")
                    .push()
                    .setValue(mensaje)

                binding.etMensaje.text.clear()
            }
        }
    }

    private fun escucharMensajes() {
        dbRef = FirebaseDatabase.getInstance()
            .getReference("chats/$chatId/messages")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mensajes.clear()
                for (msgSnap in snapshot.children) {
                    val msg = msgSnap.getValue(Mensaje::class.java)
                    if (msg != null) {
                        val msgConTipo = msg.copy(esEnviado = msg.emisor == uidActual)
                        mensajes.add(msgConTipo)
                    }
                }
                mensajeAdapter.notifyDataSetChanged()
                binding.recyclerMensajes.scrollToPosition(mensajes.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores (puedes usar Toast o Log)
            }
        })
    }
}
