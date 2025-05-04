package com.example.campusgo.Ingresar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivitySingupBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // El botón Crear Cuenta comienza deshabilitado (via XML)
        // Solo lo habilitamos cuando el usuario sube un documento.
        binding.btnUploadDoc.setOnClickListener {
            // aquí lanzarías tu selector de archivos / cámara…
            // por simplicidad, simulamos que ya subió algo y habilitamos:
            binding.btnSignUp.isEnabled = true
            Toast.makeText(this, "Documento cargado", Toast.LENGTH_SHORT).show()
        }

        binding.btnSignUp.setOnClickListener {
            val name            = binding.etName.text.toString().trim()
            val username        = binding.etUsername.text.toString().trim()
            val email           = binding.etEmail.text.toString().trim()
            val universidad     = binding.etUni.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            var valido = true

            if (name.isEmpty()) {
                binding.etName.error = getString(R.string.error_required)
                valido = false
            }
            if (username.isEmpty()) {
                binding.etUsername.error = getString(R.string.error_required)
                valido = false
            }
            if (email.isEmpty()) {
                binding.etEmail.error = getString(R.string.error_required)
                valido = false
            }
            if (universidad.isEmpty()) {
                binding.etUni.error = getString(R.string.error_required)
                valido = false
            }
            if (confirmPassword.isEmpty()) {
                binding.etConfirmPassword.error = getString(R.string.error_required)
                valido = false
            }

            if (valido) {
                // TODO: aquí iría tu lógica real de registro (Firebase, API, etc.)
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                finish() // volvemos al login
            }
        }
    }
}
