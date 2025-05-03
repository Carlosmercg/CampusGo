package com.example.campusgo.Ingresar

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.campusgo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Deshabilitamos el botón al arrancar
        binding.btnLogin.isEnabled = false

        // 2) Validamos en tiempo real
        binding.emailEditText.addTextChangedListener { validateFields() }
        binding.passwordEditText.addTextChangedListener { validateFields() }

        // 3) Acción del botón “Iniciar Sesión”
        binding.btnLogin.setOnClickListener {
            val username = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            // Simple “simulación” de login
            Toast.makeText(this, "¡Bienvenido $username!", Toast.LENGTH_SHORT).show()

            // Navegar al HomeActivity
            Intent(this, HomeActivity::class.java).also { intent ->
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }

        // 4) Redirigir al registro
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 5) Manejar acción “Done” en el teclado
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && binding.btnLogin.isEnabled) {
                binding.btnLogin.performClick()
                true
            } else {
                false
            }
        }
    }

    /** Habilita btnLogin solo si ambos campos no están vacíos */
    private fun validateFields() {
        val userNotEmpty = binding.emailEditText.text.toString().trim().isNotEmpty()
        val passNotEmpty = binding.passwordEditText.text.toString().trim().isNotEmpty()
        binding.btnLogin.isEnabled = userNotEmpty && passNotEmpty
        if (userNotEmpty)  binding.emailEditText.error = null
        if (passNotEmpty)  binding.passwordEditText.error = null
    }
}
