package com.example.campusgo.ui.venta

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityCodigoNfcCompradorBinding
import com.example.campusgo.ui.home.HomeActivity
import com.google.firebase.firestore.FirebaseFirestore


class Codigo_NFC_Comprador : AppCompatActivity() {

    private lateinit var binding: ActivityCodigoNfcCompradorBinding
    private lateinit var digitos: List<EditText>
    private lateinit var pedidoId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodigoNfcCompradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Referencias a los 6 EditText
        digitos = listOf(
            binding.et1, binding.et2, binding.et3,
            binding.et4, binding.et5, binding.et6
        )

        pedidoId = intent.getStringExtra("pedidoID").toString()

        // 2) Cada casilla acepta un solo dígito y pasa foco al siguiente
        digitos.forEachIndexed { idx, et ->
            et.filters = arrayOf(InputFilter.LengthFilter(1))
            et.isCursorVisible = false
            et.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && idx < digitos.lastIndex) {
                        digitos[idx + 1].requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        digitos[0].requestFocus()

        // 3) Al pulsar el icono, aceptamos cualquier código y vamos al Home
        binding.confirmacion.setOnClickListener {
            val ingresado = digitos.joinToString("") { it.text.toString() }
            if (ingresado.length < 6) {
                Toast.makeText(this, "Completa los 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val db = FirebaseFirestore.getInstance()
            db.collection("Pedidos").document(pedidoId).get().addOnSuccessListener { document ->
                if (document.getString("codigo") != ingresado) {
                    Toast.makeText(this, "codigo no valido", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this, "entrega confirmada! muchas gracias", Toast.LENGTH_LONG)
                        .show()
                    db.collection("Pedidos").document(pedidoId).update("estado", "terminado")
                    startActivity(Intent(this, HomeActivity::class.java))

                    finish()
                }
            }.addOnFailureListener { exception ->
                Log.e("Codigo_NFC_Comprador", "Error al obtener los datos del pedido", exception)
            }
        }
    }
}
