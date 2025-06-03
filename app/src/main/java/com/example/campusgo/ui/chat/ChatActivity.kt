package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.R
import com.example.campusgo.data.models.Mensaje
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.databinding.ActivityChatBinding
import com.example.campusgo.ui.adapters.MensajeAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
        configurarEnterParaEnviar()
    }

    private fun configurarToolbar() {
        val nombre = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        val fotoPerfilUrl = intent.getStringExtra("fotoPerfilUrl")

        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.nombreUsuarioToolbar.text = nombre

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ChatsListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

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
            enviarMensaje()
        }
    }

    private fun configurarEnterParaEnviar() {
        binding.etMensaje.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                enviarMensaje()
                true
            } else {
                false
            }
        }
    }
    private fun enviarMensaje() {
        val texto = binding.etMensaje.text.toString().trim()
        if (texto.isNotEmpty()) {
            val mensaje = Mensaje(
                contenido = texto,
                emisor = uidActual,
                receptor = uidReceptor,
                timestamp = System.currentTimeMillis()
            )

            val chatRef = FirebaseDatabase.getInstance().getReference("chats/$chatId")
            val userChatsRef = FirebaseDatabase.getInstance().getReference("usuariosChats")

            // Realtime Database: guardar mensaje y último mensaje
            chatRef.child("messages").push().setValue(mensaje)
            chatRef.child("ultimoMensaje").setValue(texto)
            userChatsRef.child(uidActual).child(chatId).setValue(true)
            userChatsRef.child(uidReceptor).child(chatId).setValue(true)

            // Firestore: ACTUALIZA listaChats para ambos usuarios
            val firestore = FirebaseFirestore.getInstance()

            val resumen = mapOf(
                "chatId" to chatId,
                "ultimoMensaje" to texto,
                "timestamp" to mensaje.timestamp
            )

            firestore.collection("usuarios")
                .document(uidActual)
                .collection("listaChats")
                .document(uidReceptor)
                .set(resumen, SetOptions.merge())

            firestore.collection("usuarios")
                .document(uidReceptor)
                .collection("listaChats")
                .document(uidActual)
                .set(resumen, SetOptions.merge())

            binding.etMensaje.text.clear()
        }
    }

    private fun escucharMensajes() {
        dbRef = FirebaseDatabase.getInstance().getReference("chats/$chatId/messages")

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
                // Manejo de error
            }
        })
    }
}
