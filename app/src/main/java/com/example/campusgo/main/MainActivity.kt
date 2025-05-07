package com.example.campusgo.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.campusgo.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.ui.auth.LoginActivity
import com.example.campusgo.databinding.ActivityMainBinding
import com.example.campusgo.ui.mapas.MapaCompradorActivity
import com.example.campusgo.ui.mapas.MapaVendedorActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ocultar la barra de acción para una experiencia completa
        supportActionBar?.hide()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Ya autenticado, ir al Home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // No autenticado, ir a login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Cierra MainActivity
        // Pasar a la pantalla de login después de 10 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finaliza MainActivity para que no vuelva atrás
        }, 2000) // 5 segundos
    }
}
