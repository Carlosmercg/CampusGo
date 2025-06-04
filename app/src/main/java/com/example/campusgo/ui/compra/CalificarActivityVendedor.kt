package com.example.campusgo.ui.compra

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.databinding.ActivityCalificarBinding
import com.example.campusgo.ui.home.HomeActivity
import com.google.firebase.firestore.FirebaseFirestore

class CalificarActivityVendedor : AppCompatActivity() {

    private lateinit var binding: ActivityCalificarBinding
    private lateinit var stars: List<ImageButton>
    private var rating = 0
    private lateinit var pedidoId : String

    private lateinit var vendedorId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pedidoId = intent.getStringExtra("pedidoID").toString()
        val db = FirebaseFirestore.getInstance()
        db.collection("Pedidos").document(pedidoId).get().addOnSuccessListener { document ->
            vendedorId = document.getString("vendedorId").toString()
        }.addOnFailureListener{
            Log.e("CalificarActivityVendedor", "Error al obtener los datos del pedido")
        }

            db.collection("usuarios")
            .document(vendedorId)
            .get()
            .addOnSuccessListener { doc ->
                doc.toObject(Usuario::class.java)?.let { usr ->
                    // Nombre completo
                    binding.textView8.text = "${usr.nombre} ${usr.apellido}"
                    // Universidad
                    binding.textView9.text = usr.universidad
                    // Foto de perfil
                    if (usr.urlFotoPerfil.isNotBlank()) {
                        Glide.with(this)
                            .load(usr.urlFotoPerfil)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(binding.imageView15)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("CalificarActivityVendedor", "Error al obtener los datos del vendedor")
            }

        stars = listOf(
            binding.btncal1,
            binding.btncal2,
            binding.btncal3,
            binding.btncal4,
            binding.btncal5
        )
        stars.forEachIndexed { index, btn ->
            btn.setOnClickListener { setRating(index + 1) }
        }

        binding.button.setOnClickListener {
            if (rating == 0) {
                Toast.makeText(this, "Selecciona una calificación", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "¡Gracias por calificar con $rating estrellas!",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setRating(value: Int) {
        rating = value
        stars.forEachIndexed { index, btn ->
            btn.setImageResource(
                if (index < value) R.drawable.star_filled
                else R.drawable.star_test
            )
        }
    }
}
