package com.example.campusgo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.campusgo.databinding.ActivityLoginBinding
import com.example.campusgo.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.btnLogin.isEnabled = false

        // Validación en tiempo real
        binding.emailEditText.addTextChangedListener { validateFields() }
        binding.passwordEditText.addTextChangedListener { validateFields() }

        // Login al presionar Enter
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && binding.btnLogin.isEnabled) {
                binding.btnLogin.performClick()
                true
            } else false
        }

        // Iniciar sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: ""
                    db.collection("usuarios").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                // Aquí añadimos la obtención y guardado del token FCM
                                FirebaseMessaging.getInstance().token
                                    .addOnSuccessListener { token ->
                                        // Guardamos el token en el documento del usuario
                                        val data = mapOf("fcmToken" to token)
                                        db.collection("usuarios")
                                            .document(uid)
                                            .set(data, SetOptions.merge())
                                            .addOnSuccessListener {
                                                // Una vez guardado, continua con la navegación
                                                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                                startActivity(
                                                    Intent(this, HomeActivity::class.java).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    }
                                                )
                                            }
                                            .addOnFailureListener { e ->
                                                // Si hubo un error guardando el token, igual continuar
                                                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                                startActivity(
                                                    Intent(this, HomeActivity::class.java).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    }
                                                )
                                            }
                                    }
                                    .addOnFailureListener {
                                        // Si no se pudo obtener el token, igualmente seguimos a HomeActivity
                                        Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                        startActivity(
                                            Intent(this, HomeActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            }
                                        )
                                    }
                            } else {
                                Toast.makeText(this, "Usuario autenticado pero no encontrado en Firestore", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al verificar datos del usuario", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { ex ->
                    when (ex) {
                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(this, "Correo no registrado", Toast.LENGTH_LONG).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(this, "Error: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }

        // Ir al registro
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    // Valida correo y contraseña
    private fun validateFields() {
        val emailValid = binding.emailEditText.text.toString().trim().let {
            it.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(it).matches()
        }
        val passwordValid = binding.passwordEditText.text.toString().trim().isNotEmpty()
        binding.btnLogin.isEnabled = emailValid && passwordValid
    }
}
