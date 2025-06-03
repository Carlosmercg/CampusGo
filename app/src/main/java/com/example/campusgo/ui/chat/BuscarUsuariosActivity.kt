package com.example.campusgo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.databinding.ActivityBuscarUsuariosBinding
import com.example.campusgo.databinding.ItemUsuarioBinding
import com.example.campusgo.ui.usuario.PerfilActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class BuscarUsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuscarUsuariosBinding
    private val resultadosBusqueda = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        setupRecycler()
        setupBuscador()
    }

    private fun configurarToolbar() {
        setSupportActionBar(binding.toolbarBuscar)
        supportActionBar?.apply {
            title = "Buscar usuarios"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
        }

        binding.toolbarBuscar.setNavigationOnClickListener {
            val intent = Intent(this, ChatsListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecycler() {
        binding.recyclerResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerResultados.adapter = UsuarioViewAdapter()
    }

    private fun setupBuscador() {
        val filtros = arrayOf("nombre", "apellido", "carrera", "universidad")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros)
        binding.spinnerFiltro.adapter = spinnerAdapter

        binding.btnBuscar.setOnClickListener {
            val texto = binding.etBuscarUsuario.query.toString().trim()
            val filtro = binding.spinnerFiltro.selectedItem.toString().lowercase()

            if (texto.length >= 2) {
                buscarUsuariosFirestore(filtro, texto)
            } else {
                resultadosBusqueda.clear()
                binding.recyclerResultados.adapter?.notifyDataSetChanged()
                Toast.makeText(this, "Escribe al menos 2 caracteres", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarUsuariosFirestore(filtro: String, valor: String) {
        val campo = when (filtro) {
            "nombre", "apellido", "carrera", "universidad" -> filtro
            else -> "nombre"
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .orderBy(campo)
            .startAt(valor)
            .endAt(valor + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                resultadosBusqueda.clear()
                resultadosBusqueda.addAll(result.toObjects(Usuario::class.java))
                binding.recyclerResultados.adapter?.notifyDataSetChanged()

                if (resultadosBusqueda.isEmpty()) {
                    Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar en Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    inner class UsuarioViewAdapter : RecyclerView.Adapter<UsuarioViewAdapter.UsuarioViewHolder>() {

        inner class UsuarioViewHolder(val item: ItemUsuarioBinding) : RecyclerView.ViewHolder(item.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
            val binding = ItemUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return UsuarioViewHolder(binding)
        }

        override fun getItemCount(): Int = resultadosBusqueda.size

        override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
            val usuario = resultadosBusqueda[position]
            holder.item.txtNombreUsuario.text = "${usuario.nombre} ${usuario.apellido}"
            holder.item.txtCarreraUsuario.text = usuario.carrera
            holder.item.txtUniversidadUsuario.text = usuario.universidad

            ManejadorImagenesAPI.mostrarImagenDesdeUrl(
                url = usuario.urlFotoPerfil,
                imageView = holder.item.imgPerfilUsuario,
                context = this@BuscarUsuariosActivity,
                placeholderRes = R.drawable.ic_profile,
                errorRes = R.drawable.ic_profile
            )

            holder.item.btnPerfil.setOnClickListener { irAlPerfil(usuario) }
            holder.item.btnChatear.setOnClickListener { iniciarChatCon(usuario) }
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

                // 🔽 AQUI PEGAS ESTA PARTE 🔽
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
                // 🔼 FIN DE LA NUEVA PARTE 🔼

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


    private fun irAlPerfil(usuario: Usuario) {
        val intent = Intent(this, PerfilActivity::class.java).apply {
            putExtra("uid", usuario.id)
        }
        startActivity(intent)
    }
}
