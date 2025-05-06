package com.example.campusgo.compra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityCodigoNfcBinding
import kotlin.random.Random

class Codigo_NFC : AppCompatActivity() {
    private lateinit var binding: ActivityCodigoNfcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodigoNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Leer el modo: "generar" o "verificar"
        val modo = intent.getStringExtra("modo") ?: "generar"

        if (modo == "generar") {
            // 1) Generar y mostrar un código aleatorio de 6 dígitos
            val codigo = Random.nextInt(100_000, 1_000_000)
                .toString()
                .padStart(6, '0')
            val partes = "${codigo.substring(0, 3)} - ${codigo.substring(3)}"
            binding.tvCodigoContainer.text = partes

            // 2) Texto de instrucción acorde
            val nombreVendedor = intent.getStringExtra("nombreVendedor") ?: "[vendedor]"
            binding.tvSubtituloConfirmacion.text =
                "Dale este código a $nombreVendedor para confirmar que recibiste el producto"
        } else {
            // Modo “verificar”: recibimos el código correcto por Intent
            val codigoCorrecto = intent.getStringExtra("codigoCorrecto")
                ?.padStart(6, '0')
                ?: "000000"
            val partes = "${codigoCorrecto.substring(0, 3)} - ${codigoCorrecto.substring(3)}"
            binding.tvCodigoContainer.text = partes

            // Ajustar subtítulo
            binding.tvSubtituloConfirmacion.text =
                "Ingresa este código en tu teléfono para confirmar la entrega"
        }

        // Siempre actualizamos el estado de “Esperando…”
        binding.tvEsperandoConfirmacion.text = "Esperando Confirmación…"
    }
}
