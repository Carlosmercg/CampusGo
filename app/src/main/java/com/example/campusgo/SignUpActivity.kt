
package com.example.campusgo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivitySingupBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Agregar fondo de imagen
        binding.root.setBackgroundResource(R.drawable.splash_background)

        // Manejo del botón de registro
        binding.btnRegistrar.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
                // Aquí iría la lógica de registro de usuario
                finish() // Volver a la pantalla de login después de registrarse
            } else {
                binding.usernameEditText.error = "Campo requerido"
                binding.passwordEditText.error = "Campo requerido"
                if (password != confirmPassword) {
                    binding.confirmPasswordEditText.error = "Las contraseñas no coinciden"
                }
            }
        }
    }
}