package com.example.campusgo.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusgo.BuildConfig
import com.example.campusgo.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ManejadorImagenesAPI(private val context: Context) {

    private val cliente = OkHttpClient()
    private val apiKey = BuildConfig.IMGBB_API_KEY

    fun subirImagen(uri: Uri, callback: (String?) -> Unit) {
        Log.d("SubidorImagenes", "🔑 Usando API KEY: $apiKey")

        // 🔒 Validación temprana de API Key
        if (apiKey.isBlank() || apiKey == "MISSING_KEY") {
            Log.e("SubidorImagenes", "❌ API Key no configurada")
            runOnUi {
                Toast.makeText(context, "❌ Error: API Key no válida. Revisa local.properties", Toast.LENGTH_LONG).show()
                callback(null)
            }
            return
        }

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File.createTempFile("upload", ".jpg", context.cacheDir)
            file.outputStream().use { inputStream?.copyTo(it) }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", apiKey)
                .addFormDataPart("image", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build()

            cliente.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("SubidorImagenes", "❌ Error de red: ${e.message}")
                    runOnUi {
                        Toast.makeText(context, "❌ Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    Log.d("SubidorImagenes", "📦 Respuesta: $responseBody")

                    if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                        runOnUi {
                            Toast.makeText(context, "❌ Respuesta inválida de imgbb", Toast.LENGTH_SHORT).show()
                            callback(null)
                        }
                        return
                    }

                    try {
                        val json = JSONObject(responseBody)
                        val url = json.getJSONObject("data").getString("url")
                        runOnUi {
                            Toast.makeText(context, "✅ Imagen subida con éxito", Toast.LENGTH_SHORT).show()
                            callback(url)
                        }
                    } catch (e: Exception) {
                        Log.e("SubidorImagenes", "❌ Error parseando JSON: ${e.message}")
                        runOnUi {
                            Toast.makeText(context, "❌ Error leyendo la respuesta", Toast.LENGTH_SHORT).show()
                            callback(null)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("SubidorImagenes", "❌ Excepción al subir imagen: ${e.message}")
            runOnUi {
                Toast.makeText(context, "❌ Error preparando imagen", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        }
    }

    private fun runOnUi(action: () -> Unit) {
        (context as? AppCompatActivity)?.runOnUiThread { action() }
    }

    companion object {
        fun mostrarImagenDesdeUrl(
            url: String?,
            imageView: ImageView,
            context: Context,
            placeholderRes: Int = R.drawable.ic_profile,
            errorRes: Int = R.drawable.ic_profile
        ) {
            if (url.isNullOrBlank()) {
                imageView.setImageResource(placeholderRes)
                Log.w("ManejadorImagenesAPI", "⚠️ URL de imagen vacía o nula.")
                return
            }

            Log.d("ManejadorImagenesAPI", "🔗 Cargando imagen desde: $url")
            Glide.with(context)
                .load(url)
                .placeholder(placeholderRes)
                .error(errorRes)
                .into(imageView)
        }
    }
}
