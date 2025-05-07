package com.example.campusgo.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import java.util.concurrent.Executors

class DescargadorImagenes(private val context: Context) {

    private val executor = Executors.newSingleThreadExecutor()

    fun cargarEn(imageView: ImageView, url: String, placeholder: Int) {
        // Mostrar placeholder de inmediato
        imageView.setImageResource(placeholder)

        executor.execute {
            try {
                val inputStream = URL(url).openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Mostrar en el hilo de UI
                runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("DescargadorImagenes", "❌ Error al descargar: ${e.message}")
                runOnUiThread {
                    Toast.makeText(context, "Error cargando imagen", Toast.LENGTH_SHORT).show()
                    imageView.setImageResource(placeholder) // Mostrar placeholder si falla
                }
            }
        }
    }

    private fun runOnUiThread(action: () -> Unit) {
        (context as? AppCompatActivity)?.runOnUiThread(action)
    }
}
