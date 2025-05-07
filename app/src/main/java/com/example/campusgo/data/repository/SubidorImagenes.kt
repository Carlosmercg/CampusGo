package com.example.campusgo.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class SubidorImagenes(private val context: Context) {

    private val cliente = OkHttpClient()
    private val apiKey = obtenerApiKeyDesdeManifest()

    fun subirImagen(uri: Uri, callback: (String?) -> Unit) {
        try {
            // Copiar el archivo del uri a un archivo temporal en cache
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

    private fun obtenerApiKeyDesdeManifest(): String {
        return try {
            val appInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            appInfo.metaData.getString("IMGBB_API_KEY") ?: ""
        } catch (e: Exception) {
            Log.e("SubidorImagenes", "❌ No se pudo obtener la API key: ${e.message}")
            ""
        }
    }
}
