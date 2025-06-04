package com.example.campusgo.ui.producto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityDetalleProductoBinding
import com.example.campusgo.data.models.Producto
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class DetalleProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleProductoBinding
    private lateinit var producto: Producto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDetalle)
        supportActionBar?.apply {
            title = getString(R.string.detalle_producto)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbarDetalle.setNavigationOnClickListener { onBackPressed() }

        producto = intent.getSerializableExtra("producto") as? Producto
            ?: run {
                Toast.makeText(this, "Producto no válido", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        mostrarProducto(producto)
        obtenerDatosVendedor(producto.vendedorId)

        binding.btnContactar.setOnClickListener {
            obtenerVendedorPorID(producto.vendedorId) { usuario ->
                if (usuario != null) {
                    iniciarChatCon(usuario)
                } else {
                    Toast.makeText(this, "No se encontró al vendedor", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnAgregarCarrito.setOnClickListener {
            agregarAlCarrito(producto)

        }
    }

    private fun mostrarProducto(p: Producto) {
        Glide.with(this)
            .load(p.imagenUrl.ifEmpty { R.drawable.ic_placeholder })
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .into(binding.imgProducto)

        binding.txtTituloProducto.text = p.nombre
        binding.txtDescripcionProducto.text = p.descripcion
        binding.txtPrecio.text = "$ ${"%,.2f".format(p.precio)}"
    }

    private fun obtenerDatosVendedor(vendedorId: String) {
        FirebaseFirestore.getInstance().collection("usuarios")
            .document(vendedorId)
            .get()
            .addOnSuccessListener { documento ->
                val vendedor = documento.toObject(Usuario::class.java)
                if (vendedor != null) {
                    binding.txtNombreVendedor.text = "${vendedor.nombre} ${vendedor.apellido}"
                    binding.txtUniversidad.text = vendedor.universidad
                } else {
                    binding.txtNombreVendedor.text = "Desconocido"
                    binding.txtUniversidad.text = "-"
                }
            }
            .addOnFailureListener {
                binding.txtNombreVendedor.text = "Error"
                binding.txtUniversidad.text = "-"
                Toast.makeText(this, "Error al cargar vendedor", Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarAlCarrito(producto: Producto) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val compradorId = auth.currentUser?.uid

        if (compradorId == null) {
            // Usuario no autenticado
            println("Error: Usuario no autenticado")
            return
        }

        // ❗ Verificar que el comprador no sea el mismo que el vendedor
        if (compradorId == producto.vendedorId) {
            Toast.makeText(this, "No puedes comprar tu propio producto", Toast.LENGTH_SHORT).show()
            return
        }

        val carritoData = hashMapOf(
            "compradorId" to compradorId,
            "imagenUrl" to producto.imagenUrl,
            "nombre" to producto.nombre,
            "precio" to producto.precio,
            "productoId" to producto.id,
            "vendedorId" to producto.vendedorId,
            "estado" to "activo"
        )

        db.collection("Carrito")
            .add(carritoData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "${producto.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${producto.nombre} Error al agregar al carrito", Toast.LENGTH_SHORT).show()
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
