// src/main/java/com/example/campusgo/compra/Codigo_NFC.kt
package com.example.campusgo.compra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityCodigoNfcBinding
import kotlin.random.Random

class Codigo_NFC : AppCompatActivity() {

    private lateinit var binding: ActivityCodigoNfcBinding
    private lateinit var codigoGenerado: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodigoNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Generar un código aleatorio de 6 dígitos y formatearlo "XXX - XXX"
        codigoGenerado = Random.nextInt(100_000, 1_000_000).toString()
        val formateado = "${codigoGenerado.substring(0, 3)} - ${codigoGenerado.substring(3)}"
        binding.tvCodigoContainer.text = formateado

        // 2) Mostramos siempre a Sara Albarracín en el subtítulo
        binding.tvSubtituloConfirmacion.text =
            "Dale este código al vendedor para confirmar que recibiste el producto"

        binding.imgSync.setOnClickListener {
            startActivity(Intent(this, CalificarActivityVendedor::class.java))
            finish()
        }
    }
}
