package com.example.campusgo.ui.compra

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusgo.data.models.Pedido
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.databinding.ActivityListaComprasPasadasBinding
import com.example.campusgo.databinding.ActivityListaProductosVendidosBinding
import com.example.campusgo.ui.adapters.PedidoAdapter
import com.example.campusgo.ui.chat.ChatActivity
import com.example.campusgo.ui.mapas.MapaCompradorActivity
import com.example.campusgo.ui.mapas.MapaVendedorActivity
import com.example.campusgo.ui.producto.DetalleProductoActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ListaComprasPasadasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaComprasPasadasBinding
    private lateinit var adapter: PedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaComprasPasadasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }
        // Set the layout manager here
        binding.productos.layoutManager = LinearLayoutManager(this)
        //Create the adapter
        adapter = PedidoAdapter(mutableListOf()) { pedido ->
            when (pedido.estado) {
                "aceptado" -> {
                    val intent = Intent(this, MapaCompradorActivity::class.java)
                    intent.putExtra("pedidoID", pedido.id)
                    startActivity(intent)
                }

                "activo" -> {
                    obtenerVendedorPorID(pedido.vendedorId) { usuario ->
                        if (usuario != null) {
                            iniciarChatCon(usuario)
                        } else {
                            Toast.makeText(this, "No se encontró al vendedor", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                "terminado" -> {
                    Toast.makeText(this, "Este pedido ya fue terminado", Toast.LENGTH_SHORT).show()
                }

                "rechazado" -> {
                    Toast.makeText(this, "Este pedido fue rechazado", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Estado desconocido del pedido", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //set the adapter to the recycler view
        binding.productos.adapter = adapter

        inicializarLista()

    }

    private fun inicializarLista() {
        val db = FirebaseFirestore.getInstance()
        val usuario = Firebase.auth.currentUser
        db.collection("Pedidos")
            .whereEqualTo("compradorId", usuario?.uid)
            .get()
            .addOnSuccessListener { result ->
                val pedidos = mutableListOf<Pedido>()
                for (document in result) {
                    pedidos.add(document.toObject(Pedido::class.java))
                }
                // Update the data in the adapter using the correct method
                adapter.updateData(pedidos)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al leer productos", exception)
            }
    }

    private fun obtenerVendedorPorID(vendedorID: String, callback: (Usuario?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(vendedorID).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val usuario = documento.toObject(Usuario::class.java)?.copy(id = documento.id)
                    callback(usuario)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    private fun iniciarChatCon(usuario: Usuario) {
        val uidActual = FirebaseAuth.getInstance().uid

        if (uidActual == null) {
            Toast.makeText(this, "Debes iniciar sesión para usar el chat", Toast.LENGTH_SHORT).show()
            return
        }

        if (usuario.id.isEmpty()) {
            Toast.makeText(this, "El usuario no tiene un ID válido", Toast.LENGTH_SHORT).show()
            return
        }


        if (usuario.id == uidActual) {
            Toast.makeText(this, "No puedes chatear contigo mismo", Toast.LENGTH_SHORT).show()
            return
        }


        val chatId = if (uidActual < usuario.id) "$uidActual-${usuario.id}" else "${usuario.id}-$uidActual"
        val db = FirebaseDatabase.getInstance()

        db.getReference("chats/$chatId/participantes")
            .setValue(mapOf(uidActual to true, usuario.id to true))
            .addOnSuccessListener {
                db.getReference("usuariosChats/$uidActual/$chatId").setValue(true)
                db.getReference("usuariosChats/${usuario.id}/$chatId").setValue(true)

                val dbFirestore = FirebaseFirestore.getInstance()

                dbFirestore.collection("usuarios")
                    .document(uidActual)
                    .collection("listaChats")
                    .document(usuario.id)
                    .set(
                        mapOf(
                            "chatId" to chatId,
                            "nombre" to usuario.nombre,
                            "apellido" to usuario.apellido,
                            "fotoPerfil" to usuario.urlFotoPerfil
                        )
                    )

                dbFirestore.collection("usuarios")
                    .document(usuario.id)
                    .collection("listaChats")
                    .document(uidActual)
                    .set(
                        mapOf(
                            "chatId" to chatId,
                            "nombre" to FirebaseAuth.getInstance().currentUser?.displayName,
                            "fotoPerfil" to "" // Puedes completar si tienes la URL
                        )
                    )

                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("uidReceptor", usuario.id)
                    putExtra("nombreUsuario", usuario.nombre)
                    putExtra("fotoPerfilUrl", usuario.urlFotoPerfil)
                }
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al iniciar el chat: ${it.message}", Toast.LENGTH_LONG).show()
                it.printStackTrace()
                }
        }

}