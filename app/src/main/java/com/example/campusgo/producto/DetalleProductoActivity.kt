package com.example.campusgo.ui.producto

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityDetalleProductoBinding
import com.example.campusgo.data.models.Producto
import com.example.campusgo.data.models.Usuario
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
            Toast.makeText(this, "Funcionalidad de chat pendiente", Toast.LENGTH_SHORT).show()
        }

        binding.btnAgregarCarrito.setOnClickListener {
            Toast.makeText(this, "${producto.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
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
}
