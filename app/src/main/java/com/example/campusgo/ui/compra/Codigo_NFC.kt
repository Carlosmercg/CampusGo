// src/main/java/com/example/campusgo/compra/Codigo_NFC.kt
package com.example.campusgo.ui.compra

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityCodigoNfcBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class Codigo_NFC : AppCompatActivity() {

    private lateinit var binding: ActivityCodigoNfcBinding
    private lateinit var codigoGenerado: String
    private lateinit var pedidoId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodigoNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pedidoId = intent.getStringExtra("pedidoID").toString()

        // 1) Generar un código aleatorio de 6 dígitos y formatearlo "XXX - XXX"
        codigoGenerado = Random.nextInt(100_000, 1_000_000).toString()
        val formateado = "${codigoGenerado.substring(0, 3)} - ${codigoGenerado.substring(3)}"
        binding.tvCodigoContainer.text = formateado
        val db = FirebaseFirestore.getInstance()
        db.collection("Pedidos").document(pedidoId).update("codigo", codigoGenerado)

        // 2) Mostramos siempre a Sara Albarracín en el subtítulo
        binding.tvSubtituloConfirmacion.text =
            "Dale este código al vendedor para confirmar que recibiste el producto"

        binding.confirmacion.setOnClickListener {
            db.collection("Pedidos").document(pedidoId).get().addOnSuccessListener { document ->
                if (document.getString("estado") == "terminado") {
                    Toast.makeText(this, "gracias por compartir el codigo!", Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(this, CalificarActivityVendedor::class.java)
                    intent.putExtra("pedidoID", pedidoId)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "codigo no valido", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("Codigo_NFC", "Error al obtener los datos del pedido", exception)
            }
        }
    }
}
