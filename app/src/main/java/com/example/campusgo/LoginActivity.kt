package com.example.campusgo
//Inicio de Sesión / Registro? → Pantalla para iniciar sesión o registrarse.
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
