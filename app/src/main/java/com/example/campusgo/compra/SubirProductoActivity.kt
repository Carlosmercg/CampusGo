package com.example.campusgo.compra

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivitySubirProductoBinding

class SubirProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubirProductoBinding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubirProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }
        binding.guardar.setOnClickListener {
            finish()
        }

        binding.fotoProducto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            seleccionarImagenLauncher.launch(intent)
        }


    }

    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                mostrarImagenSeleccionada(it)
            }
        }
    }

    private fun mostrarImagenSeleccionada(uri: Uri) {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
        binding.fotoProducto.setImageBitmap(bitmap)
    }
}