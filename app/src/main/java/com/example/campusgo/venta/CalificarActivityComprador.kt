package com.example.campusgo.ui.venta

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityCalificarBinding
import com.example.campusgo.databinding.ActivityCalificarCompradorBinding
import com.example.campusgo.ui.home.HomeActivity

import com.google.firebase.firestore.FirebaseFirestore

class CalificarActivityComprador : AppCompatActivity() {

    private lateinit var binding: ActivityCalificarCompradorBinding
    private lateinit var stars: List<ImageButton>
    private var rating = 0

    private val vendedorId = "jY6VoRW0pLhM1Yq4MSECkeoSs3r2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificarCompradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(vendedorId)
            .get()
            .addOnSuccessListener { doc ->
                doc.toObject(Usuario::class.java)?.let { usr ->
                    // Nombre completo
                    binding.textView8.text = "${usr.nombre} ${usr.apellido}"
                    // Universidad
                    binding.textView9.text = usr.universidad
                    // Foto de perfil
                    if (usr.fotoPerfilUrl.isNotBlank()) {
                        Glide.with(this)
                            .load(usr.fotoPerfilUrl)
                            .placeholder(R.drawable.placeholder_usuario)
                            .error(R.drawable.placeholder_usuario)
                            .into(binding.imageView15)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando datos del vendedor", Toast.LENGTH_SHORT).show()
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
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
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
