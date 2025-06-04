package com.example.campusgo.ui.auth

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
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
    private lateinit var prefs: SharedPreferences
    private val db by lazy { FirebaseFirestore.getInstance() }


    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
            } else {
                Toast.makeText(
                    this,
                    "Para recibir notificaciones de chat, habilita el permiso",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)

        binding.btnLogin.isEnabled = false

        // 2) Pidamos permiso si es Android 13+ y no está concedido
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        this,
                        "La app necesita permiso de notificaciones para avisarte de nuevos mensajes",
                        Toast.LENGTH_LONG
                    ).show()
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        configurarListeners()
        configurarBotonHuella()
    }

    private fun configurarListeners() {
        binding.emailEditText.addTextChangedListener { validateFields() }
        binding.passwordEditText.addTextChangedListener { validateFields() }

        // Login al presionar Enter
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
                            // Obtener y guardar token FCM
                            FirebaseMessaging.getInstance().token
                                .addOnSuccessListener { token ->
                                    val data = mapOf("fcmToken" to token)
                                    db.collection("usuarios")
                                        .document(uid)
                                        .set(data, SetOptions.merge())
                                        .addOnSuccessListener {
                                            irAHOME()
                                        }
                                        .addOnFailureListener {
                                            irAHOME()
                                        }
                                }
                                .addOnFailureListener {
                                    irAHOME()
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Usuario autenticado pero no registrado en Firestore",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Error al verificar datos del usuario",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { ex ->
                val mensaje = when (ex) {
                    is FirebaseAuthInvalidUserException      -> "Correo no registrado"
                    is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta"
                    else                                      -> "Error: ${ex.localizedMessage}"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarCredenciales(email: String, password: String) {
        prefs.edit()
            .putString("email", email)
            .putString("password", password) // Idealmente cifrar
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
                    Toast.makeText(applicationContext, "Huella verificada", Toast.LENGTH_SHORT)
                        .show()

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            irAHOME()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                applicationContext,
                                "Error al iniciar sesión",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        val promptInfo: PromptInfo = PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Coloca tu huella ahora")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun irAHOME() {
        startActivity(
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
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
