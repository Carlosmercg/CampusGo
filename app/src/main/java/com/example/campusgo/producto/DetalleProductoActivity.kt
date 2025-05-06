package com.example.campusgo.producto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.chat.ChatActivity
import com.example.campusgo.databinding.ActivityDetalleProductoBinding
import com.example.campusgo.models.Producto
import com.example.campusgo.models.Usuario
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

        val id             = intent.getStringExtra("productoId")          ?: ""
        val nombre         = intent.getStringExtra("productoNombre")      ?: ""
        val descripcion    = intent.getStringExtra("productoDescripcion") ?: ""
        val precio         = intent.getDoubleExtra("productoPrecio", 0.0)
        val imagenUrl      = intent.getStringExtra("productoImagenUrl")   ?: ""
        val vendedorId     = intent.getStringExtra("vendedorId")         ?: ""
        val vendedorNombre = intent.getStringExtra("vendedorNombre")     ?: ""

        producto = Producto(
            id             = id,
            nombre         = nombre,
            vendedorId     = vendedorId,
            precio         = precio,
            imagenUrl      = imagenUrl,
            vendedorNombre = vendedorNombre,
            descripcion    = descripcion
        )


        mostrarProducto(producto)
        obtenerDatosVendedor(producto.vendedorId)

        binding.btnContactar.setOnClickListener {
            Intent(this, ChatActivity::class.java).apply {
                putExtra("chatId",        producto.vendedorId)
                putExtra("nombreUsuario", producto.vendedorNombre)
            }.also(::startActivity)
        }

        binding.btnAgregarCarrito.setOnClickListener {
            val carritoRef = FirebaseFirestore.getInstance().collection("Carrito")
            val map = mapOf(
                "productoId" to producto.id,
                "nombre"      to producto.nombre,
                "precio"      to producto.precio,
                "imagenUrl"   to producto.imagenUrl,
                "vendedorId"  to producto.vendedorId
            )
            carritoRef.add(map)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "${producto.nombre} añadido al carrito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error al añadir al carrito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun mostrarProducto(p: Producto) {
        // Cargar imagen
        if (p.imagenUrl.isNotBlank()) {
            Glide.with(this)
                .load(p.imagenUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(binding.imgProducto)
        } else {
            binding.imgProducto.setImageResource(R.drawable.ic_placeholder)
        }

        binding.txtTituloProducto.text    = p.nombre
        binding.txtDescripcionProducto.text = p.descripcion
        binding.txtPrecio.text            = "$ ${"%,.2f".format(p.precio)}"
    }

    private fun obtenerDatosVendedor(vendedorId: String) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(vendedorId)
            .get()
            .addOnSuccessListener { doc ->
                doc.toObject(Usuario::class.java)?.let { usr ->
                    binding.txtNombreVendedor.text = "${usr.nombre} ${usr.apellido}"
                    binding.txtUniversidad.text    = usr.universidad
                } ?: run {
                    binding.txtNombreVendedor.text = "Desconocido"
                    binding.txtUniversidad.text    = "-"
                }
            }
            .addOnFailureListener {
                binding.txtNombreVendedor.text = "Error"
                binding.txtUniversidad.text    = "-"
                Toast.makeText(this, "Error cargando vendedor", Toast.LENGTH_SHORT).show()
            }
    }
}
