package com.example.campusgo.Ingresar

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.campusgo.databinding.ActivitySingupBinding
import com.example.campusgo.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    private lateinit var auth: FirebaseAuth
    private val storageRef by lazy { FirebaseStorage.getInstance().reference }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var carnetUri: Uri? = null
    private var photoFile: File? = null
    private lateinit var uriCamera: Uri

    private val universidades = listOf(
        "Pontificia Universidad Javeriana",
        "Universidad Católica",
        "Universidad Distrital Francisco José de Caldas",
        "Universidad Santo Tomás",
        "Universidad ECCI"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Adaptador de universidades con bloqueo manual
        val adapterUni = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, universidades)
        binding.etUni.setAdapter(adapterUni)
        binding.etUni.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.etUni.showDropDown()
            }
            true
        }

        // Galería
        val pickGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                carnetUri = uri
                mostrarImagen(uri)
            }
        }

        // Cámara
        val takeCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoFile?.exists() == true && photoFile?.length() ?: 0L > 0L) {
                carnetUri = uriCamera
                mostrarImagen(uriCamera)
            } else {
                Toast.makeText(this, "Error: la foto no se guardó correctamente", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnUploadGaleria.setOnClickListener {
            solicitarPermisoGaleria { pickGaleria.launch("image/*") }
        }

        binding.btnUploadDocCamara.setOnClickListener {
            solicitarPermisoCamara {
                photoFile = File(getExternalFilesDir(null), "carnet_${System.currentTimeMillis()}.jpg")
                uriCamera = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile!!)
                takeCamera.launch(uriCamera)
            }
        }

        binding.btnSignUp.setOnClickListener {
            val nombre = binding.etName.text.toString().trim()
            val apellido = binding.etApellido.text.toString().trim()
            val universidad = binding.etUni.text.toString().trim()
            val correo = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (!validar(nombre, apellido, universidad, correo, password, confirmPassword)) return@setOnClickListener

            if (carnetUri == null) {
                Toast.makeText(this, "Debes subir una imagen del carné", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            subirCarnetYCrearUsuario(nombre, apellido, universidad, correo, password)
        }
    }

    private fun mostrarImagen(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            binding.imgPreview.setImageBitmap(bitmap)
            binding.btnSignUp.isEnabled = true
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun solicitarPermisoGaleria(onGranted: () -> Unit) {
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permiso), 100)
            this.onGaleriaPermitida = onGranted
        } else {
            onGranted()
        }
    }

    private fun solicitarPermisoCamara(onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
            this.onCamaraPermitida = onGranted
        } else {
            onGranted()
        }
    }

    private var onGaleriaPermitida: (() -> Unit)? = null
    private var onCamaraPermitida: (() -> Unit)? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                100 -> onGaleriaPermitida?.invoke()
                101 -> onCamaraPermitida?.invoke()
            }
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validar(nombre: String, apellido: String, universidad: String, correo: String, password: String, confirmPassword: String): Boolean {
        var valido = true
        if (nombre.isEmpty()) { binding.etName.error = "Requerido"; valido = false }
        if (apellido.isEmpty()) { binding.etApellido.error = "Requerido"; valido = false }
        if (universidad.isEmpty()) { binding.etUni.error = "Requerido"; valido = false }
        else if (!universidades.contains(universidad)) {
            binding.etUni.error = "Selecciona una universidad válida"; valido = false
        }
        if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.etEmail.error = "Correo inválido"; valido = false
        }
        if (password.isEmpty()) { binding.etPassword.error = "Requerido"; valido = false }
        if (confirmPassword.isEmpty()) { binding.etConfirmPassword.error = "Requerido"; valido = false }
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            valido = false
        }
        return valido
    }

    private fun subirCarnetYCrearUsuario(nombre: String, apellido: String, universidad: String, correo: String, password: String) {
        val nombreArchivo = "carnets/${UUID.randomUUID()}.jpg"
        carnetUri?.let { uri ->
            storageRef.child(nombreArchivo).putFile(uri)
                .addOnSuccessListener {
                    storageRef.child(nombreArchivo).downloadUrl.addOnSuccessListener { url ->
                        crearUsuarioFirebase(nombre, apellido, universidad, correo, password, url.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir carné: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun crearUsuarioFirebase(nombre: String, apellido: String, universidad: String, correo: String, password: String, carnetUrl: String) {
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                val usuario = Usuario(
                    id = uid,
                    nombre = nombre,
                    apellido = apellido,
                    universidad = universidad,
                    correo = correo,
                    fotoCarnetUrl = carnetUrl,
                    fotoPerfilUrl = "",
                    compras = emptyList(),
                    ventas = emptyList(),
                    chats = emptyList()
                )
                db.collection("usuarios").document(uid).set(usuario)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrar: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
