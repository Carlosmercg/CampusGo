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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                enviarMensaje()
                true
            } else {
                false
            }
        }
    }

    private fun enviarMensaje() {
        val texto = binding.etMensaje.text.toString().trim()
        if (texto.isEmpty()) return

        // 1) Construir el objeto Mensaje (se asume que tu data class Mensaje ya tiene campos: contenido, emisor, receptor, timestamp)
        val mensaje = Mensaje(
            contenido = texto,
            emisor    = uidActual,
            receptor  = uidReceptor,
            timestamp = System.currentTimeMillis()
        )

        // 2) Referencias a Realtime Database:
        //    ANTES estabas usando "chats/$chatId/mensajes". Debe cambiarse a "chats/$chatId/messages"
        val chatRef      = FirebaseDatabase.getInstance().getReference("chats/$chatId")
        val userChatsRef = FirebaseDatabase.getInstance().getReference("usuariosChats")

        // 3) Guardar en Realtime Database en la rama "messages"
        chatRef.child("messages").push().setValue(mensaje)
        chatRef.child("ultimoMensaje").setValue(texto)
        userChatsRef.child(uidActual).child(chatId).setValue(true)
        userChatsRef.child(uidReceptor).child(chatId).setValue(true)

        // 4) Actualizar listaChats en Firestore (sin cambios)
        val firestore = FirebaseFirestore.getInstance()
        val resumen = mapOf(
            "chatId"        to chatId,
            "ultimoMensaje" to texto,
            "timestamp"     to mensaje.timestamp
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

        // 5) Limpiar el EditText
        binding.etMensaje.text.clear()

        // 6) Empujar el nodo de notificación para el receptor
        //    (aunque ya no se use “notificacionesChat” si usas Cloud Function,
        //     lo dejamos igual o puedes eliminarlo si solo dependes de FCM)
        val notisRef = FirebaseDatabase.getInstance()
            .getReference("notificacionesChat")
            .child(uidReceptor)

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uidActual)
            .get()
            .addOnSuccessListener { docEmisor ->
                val nombreEmisor = docEmisor.getString("nombre") ?: "Usuario"
                val noti = hashMapOf<String, Any>(
                    "tipo" to "chat",
                    "chatId" to chatId,
                    "emisorId" to uidActual,
                    "title" to "$nombreEmisor te ha enviado un mensaje",
                    "body" to texto,
                    "timestamp" to System.currentTimeMillis()
                )
                notisRef.push().setValue(noti)
            }
            .addOnFailureListener {
                val noti = hashMapOf<String, Any>(
                    "tipo" to "chat",
                    "chatId" to chatId,
                    "emisorId" to uidActual,
                    "title" to "Usuario te ha enviado un mensaje",
                    "body" to texto,
                    "timestamp" to System.currentTimeMillis()
                )
                notisRef.push().setValue(noti)
            }
    }

    private fun escucharMensajes() {
        // 1) Cambiamos “mensajes” a “messages” para que coincida con Cloud Function
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
