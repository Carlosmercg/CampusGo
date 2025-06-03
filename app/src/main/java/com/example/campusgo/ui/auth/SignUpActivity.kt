package com.example.campusgo.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.data.repository.ManejadorImagenesAPI
import com.example.campusgo.data.repository.UsuarioRepository
import com.example.campusgo.databinding.ActivitySingupBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    private lateinit var auth: FirebaseAuth
    private val usuarioRepository = UsuarioRepository()
    private lateinit var manejadorImagenesAPI: ManejadorImagenesAPI

    private lateinit var launcherGaleria: ActivityResultLauncher<String>
    private lateinit var launcherCamara: ActivityResultLauncher<Uri>
    private lateinit var uriCamara: Uri

    private var uriCarnet: Uri? = null
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        manejadorImagenesAPI = ManejadorImagenesAPI(this)

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        val universidades = listOf(
            "Universidad de Colombia",
            "Pontificia Universidad Javeriana",
            "Universidad Católica",
            "Universidad Distrital Francisco José de Caldas",
            "Universidad Santo Tomás",
            "Universidad ECCI"
        )

        binding.etUni.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, universidades))

        configurarGaleria()
        configurarCamara()

        binding.btnUploadGaleria.setOnClickListener { solicitarPermisoGaleria() }
        binding.btnUploadDocCamara.setOnClickListener { solicitarPermisoCamara() }

        binding.btnSignUp.setOnClickListener {
            val universidadIngresada = binding.etUni.text.toString().trim()
            if (!universidades.contains(universidadIngresada)) {
                mostrar("Selecciona una universidad válida de la lista")
                return@setOnClickListener
            }
            if (uriCarnet == null) {
                mostrar("Debes subir una foto del carné")
                return@setOnClickListener
            }
            subirImagenYValidar()
        }
    }

    private fun solicitarPermisoGaleria() {
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permiso), 100)
        } else {
            launcherGaleria.launch("image/*")
        }
    }

    private fun solicitarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            abrirCamara()
        }
    }

    private fun configurarGaleria() {
        launcherGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                uriCarnet = it
                mostrarImagen(it)
            }
        }
    }

    private fun configurarCamara() {
        launcherCamara = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                uriCarnet = uriCamara
                mostrarImagen(uriCamara)
            } else {
                mostrar("Foto cancelada")
            }
        }
    }

    private fun abrirCamara() {
        val archivo = File(getExternalFilesDir(null), "carnet_temp.jpg")
        uriCamara = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)
        launcherCamara.launch(uriCamara)
    }

    private fun mostrarImagen(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            binding.imgPreview.setImageBitmap(bitmap)
        } catch (e: Exception) {
            mostrar("Error al cargar imagen")
            Log.e(TAG, "Error mostrando imagen: ${e.message}")
        }
    }

    private fun subirImagenYValidar() {
        binding.progressBar.visibility = View.VISIBLE
        mostrar("⏳ Subiendo imagen del carné...")

        manejadorImagenesAPI.subirImagen(uriCarnet!!) { urlCarnet ->
            if (urlCarnet.isNullOrEmpty()) {
                mostrar("❌ No se pudo subir la imagen del carné")
                binding.progressBar.visibility = View.GONE
                return@subirImagen
            }

            mostrar("✅ Imagen subida correctamente. URL recibida.")
            Log.d(TAG, "URL recibida de imgbb: $urlCarnet")

            val correo = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()
            val nombre = binding.etNombre.text.toString().trim()
            val apellido = binding.etApellido.text.toString().trim()
            val universidad = binding.etUni.text.toString().trim()

            if (!validarCampos(correo, pass, confirm, nombre, apellido, universidad, urlCarnet)) {
                binding.progressBar.visibility = View.GONE
                return@subirImagen
            }

            crearUsuario(correo, pass, nombre, apellido, universidad, urlCarnet)
        }
    }

    private fun crearUsuario(
        correo: String,
        pass: String,
        nombre: String,
        apellido: String,
        universidad: String,
        urlCarnet: String
    ) {
        auth.createUserWithEmailAndPassword(correo, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                mostrar("Usuario registrado correctamente en Auth ✅")

                val usuario = Usuario(
                    id = uid,
                    nombre = nombre,
                    apellido = apellido,
                    correo = correo,
                    universidad = universidad,
                    urlCarnet = urlCarnet // ✅ Asignar URL subida al campo de foto
                )

                usuarioRepository.registrarUsuario(
                    usuario,
                    onSuccess = {
                        mostrar("Usuario registrado correctamente en Firestore ✅")
                        finish()
                    },
                    onError = {
                        mostrar("Error al guardar usuario: $it")
                    }
                )
            }
            .addOnFailureListener {
                mostrar("Error al registrar en Auth: ${it.message}")
                Log.e(TAG, "Error creando usuario", it)
            }
            .addOnCompleteListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun validarCampos(
        correo: String,
        pass: String,
        confirm: String,
        nombre: String,
        apellido: String,
        universidad: String,
        urlCarnet: String
    ): Boolean {
        return when {
            correo.isEmpty() || pass.isEmpty() || confirm.isEmpty() || nombre.isEmpty() || universidad.isEmpty() || apellido.isEmpty() || urlCarnet.isEmpty() -> {
                mostrar("Completa todos los campos y sube tu carné")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                mostrar("Correo inválido")
                false
            }
            pass.length < 6 -> {
                mostrar("La contraseña debe tener al menos 6 caracteres")
                false
            }
            pass != confirm -> {
                mostrar("Las contraseñas no coinciden")
                false
            }
            else -> true
        }
    }

    private fun mostrar(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Log.d(TAG, msg)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                launcherGaleria.launch("image/*")
            } else {
                mostrar("Permiso denegado para galería")
            }
            101 -> if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                mostrar("Permiso denegado para cámara")
            }
        }
    }
}
