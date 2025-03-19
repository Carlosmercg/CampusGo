package com.example.campusgo
//Mensajería → Lista de chats organizados en pestañas (Clientes y Vendedores).
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityMensajeriaBinding

class MensajeriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMensajeriaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMensajeriaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
