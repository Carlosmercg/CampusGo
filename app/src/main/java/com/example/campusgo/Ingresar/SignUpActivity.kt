package com.example.campusgo.Ingresar

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    private lateinit var auth: FirebaseAuth
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var carnetUri: Uri? = null
    private var archivoCamara: File? = null

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

        configurarPermisos()
        configurarSelectorUniversidades()
        configurarBotones()
    }

    private fun configurarPermisos() {
        val permisos = mutableListOf(Manifest.permission.CAMERA)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        permisos.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(it), 0)
            }
        }
    }

    private fun configurarSelectorUniversidades() {
        val adaptador = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, universidades)
        binding.etUni.setAdapter(adaptador)
        binding.etUni.setOnTouchListener { view, event ->
            view.performClick()
            binding.etUni.showDropDown()
            true
        }
    }

    private fun configurarBotones() {
        val cargarGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                carnetUri = it
                mostrarImagen(it)
                mostrarToast("Imagen seleccionada desde galería")
            }
        }

        val tomarFoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { fueExitosa ->
            if (fueExitosa && archivoCamara?.exists() == true) {
                carnetUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivoCamara!!)
                mostrarImagen(carnetUri!!)
                mostrarToast("Foto tomada correctamente")
            } else {
                mostrarToast("Error al tomar la foto")
            }
        }

        binding.btnUploadGaleria.setOnClickListener {
            cargarGaleria.launch("image/*")
        }

        binding.btnUploadDocCamara.setOnClickListener {
            archivoCamara = File(getExternalFilesDir(null), "carnet_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivoCamara!!)
            tomarFoto.launch(uri)
        }

        binding.btnSignUp.setOnClickListener {
            val usuario = recolectarDatos() ?: return@setOnClickListener
            if (carnetUri == null) {
                mostrarToast("Debes subir una imagen de tu carné")
                return@setOnClickListener
            }

            mostrarToast("Registrando usuario...")
            crearUsuarioEnAuth(usuario, carnetUri!!)
        }
    }

    private fun recolectarDatos(): Usuario? {
        val nombre = binding.etName.text.toString().trim()
        val apellido = binding.etApellido.text.toString().trim()
        val universidad = binding.etUni.text.toString().trim()
        val correo = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmar = binding.etConfirmPassword.text.toString().trim()

        return when {
            nombre.isEmpty() || apellido.isEmpty() || universidad.isEmpty() -> {
                mostrarToast("Completa todos los campos"); null
            }
            !universidades.contains(universidad) -> {
                mostrarToast("Selecciona una universidad válida"); null
            }
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                mostrarToast("Correo inválido"); null
            }
            password.length < 6 || password != confirmar -> {
                mostrarToast("Contraseña inválida o no coinciden"); null
            }
            else -> Usuario(nombre, apellido, universidad, correo, password)
        }
    }

    private fun crearUsuarioEnAuth(usuario: Usuario, uri: Uri) {
        auth.createUserWithEmailAndPassword(usuario.correo, usuario.password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid ?: return@addOnSuccessListener mostrarToast("UID es null")
                subirImagen(uri) { urlImagen ->
                    val usuarioFinal = usuario.copy(id = uid, fotoCarnetUrl = urlImagen)
                    registrarEnFirestore(usuarioFinal)
                }
            }
            .addOnFailureListener {
                mostrarToast("Error creando usuario: ${it.message}")
            }
    }

    private fun subirImagen(uri: Uri, onUrlObtenida: (String) -> Unit) {
        val key = applicationContext.packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData.getString("IMGBB_API_KEY")

        if (key.isNullOrEmpty()) {
            mostrarToast("API Key imgbb no configurada")
            return
        }

        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val archivoTemp = File.createTempFile("temp_img", ".jpg", cacheDir)
            FileOutputStream(archivoTemp).use { output -> inputStream.copyTo(output) }

            val cuerpo = archivoTemp.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", archivoTemp.name, cuerpo).build()

            val solicitud = Request.Builder()
                .url("https://api.imgbb.com/1/upload?key=$key")
                .post(multipart)
                .build()

            Thread {
                try {
                    val respuesta = OkHttpClient().newCall(solicitud).execute()
                    val cuerpoTexto = respuesta.body?.string() ?: return@Thread
                    val urlImagen = JSONObject(cuerpoTexto).getJSONObject("data").getString("url")

                    runOnUiThread {
                        mostrarToast("Imagen subida con éxito")
                        onUrlObtenida(urlImagen)
                    }
                } catch (e: Exception) {
                    runOnUiThread { mostrarToast("Error subiendo imagen: ${e.message}") }
                }
            }.start()

        } catch (e: Exception) {
            mostrarToast("Error preparando imagen: ${e.message}")
        }
    }

    private fun registrarEnFirestore(usuario: Usuario) {
        db.collection("usuarios").document(usuario.id).set(usuario)
            .addOnSuccessListener {
                mostrarToast("🎉 Cuenta registrada correctamente")
                finish()
            }
            .addOnFailureListener {
                mostrarToast("Error al guardar usuario: ${it.message}")
            }
    }

    private fun mostrarImagen(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use {
                val bitmap = BitmapFactory.decodeStream(it)
                binding.imgPreview.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            mostrarToast("Error al mostrar la imagen")
        }
    }

    private fun mostrarToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Log.d("SignUpActivity", msg)
    }
}
