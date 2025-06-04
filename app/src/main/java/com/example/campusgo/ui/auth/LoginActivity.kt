package com.example.campusgo.ui.auth

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.campusgo.databinding.ActivityLoginBinding
import com.example.campusgo.ui.home.HomeActivity
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)

        binding.btnLogin.isEnabled = false

        configurarListeners()
        configurarBotonHuella()
    }

    private fun configurarListeners() {
        binding.emailEditText.addTextChangedListener { validateFields() }
        binding.passwordEditText.addTextChangedListener { validateFields() }

        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && binding.btnLogin.isEnabled) {
                binding.btnLogin.performClick()
                true
            } else false
        }

        binding.btnLogin.setOnClickListener { loginConEmail() }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginConEmail() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid.orEmpty()
                guardarCredenciales(email, password)

                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                            irAHOME()
                        } else {
                            Toast.makeText(this, "Usuario autenticado pero no registrado en Firestore", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al verificar los datos del usuario", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { ex ->
                val mensaje = when (ex) {
                    is FirebaseAuthInvalidUserException -> "Correo no registrado"
                    is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta"
                    else -> "Error: ${ex.localizedMessage}"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarCredenciales(email: String, password: String) {
        prefs.edit()
            .putString("email", email)
            .putString("password", password) // Nota: Idealmente deberías cifrar esto
            .apply()
    }

    private fun configurarBotonHuella() {
        val emailGuardado = prefs.getString("email", null)
        val passwordGuardado = prefs.getString("password", null)

        if (emailGuardado.isNullOrEmpty() || passwordGuardado.isNullOrEmpty()) {
            binding.btnFingerprintLogin?.setOnClickListener {
                Toast.makeText(
                    this,
                    "Primero debes iniciar sesión con correo y contraseña",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            binding.btnFingerprintLogin?.setOnClickListener {
                mostrarLoginBiometrico(emailGuardado, passwordGuardado)
            }
        }
    }

    private fun mostrarLoginBiometrico(email: String, password: String) {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(
            this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Huella verificada", Toast.LENGTH_SHORT).show()

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            irAHOME()
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Coloca tu huella ahora")
            .setNegativeButtonText("Cancelar")
            .build()

        // Mostrar un diálogo previo para prepararse
        android.app.AlertDialog.Builder(this)
            .setTitle("Preparar autenticación")
            .setMessage("Presiona 'Continuar' y espera 2 segundos para activar el sensor.")
            .setPositiveButton("Continuar") { dialog, _ ->
                dialog.dismiss()

                val progress = ProgressDialog(this)
                progress.setMessage("Iniciando sensor de huella...")
                progress.setCancelable(false)
                progress.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    progress.dismiss()
                    Log.d("LoginActivity", "Mostrando prompt biométrico...")
                    biometricPrompt.authenticate(promptInfo)
                }, 2000)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun irAHOME() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun validateFields() {
        val emailValid = binding.emailEditText.text.toString().trim()
            .let { it.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(it).matches() }

        val passwordValid = binding.passwordEditText.text.toString().trim().isNotEmpty()

        binding.btnLogin.isEnabled = emailValid && passwordValid
    }
}
