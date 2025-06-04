// src/main/java/com/example/campusgo/services/FcmService.kt
package com.example.campusgo.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.campusgo.R
import com.example.campusgo.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FcmService"
        private const val CHANNEL_ID = "canal_chat_mensajes"
    }

    /**
     * Se dispara cada vez que el token FCM cambia (o se genera por primera vez).
     * Lo guardamos en Firestore en `usuarios/{uid}.fcmToken`.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")

        // Si el usuario ya está autenticado, actualiza el token en Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val db = Firebase.firestore
            val data = mapOf("fcmToken" to token)
            db.collection("usuarios")
                .document(user.uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Token FCM actualizado en Firestore para ${user.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error actualizando token FCM", e)
                }
        }
    }

    /**
     * Se dispara cuando llega un mensaje FCM con payload (Data Message) y la app está en
     * primer plano, o cuando la app está en segundo plano (si va en su propio canal).
     *
     * La Cloud Function (v1) que subiste en Firebase:
     *   functions.database.ref("/chats/{chatId}/messages/{messageId}").onCreate(...)
     * envía siempre un Data Message con keys:
     *   "titulo", "cuerpo", "nombreEmisor", "ultimoMensaje", "chatId", "uidEmisor"
     *
     * Aquí capturamos ese map de .data y mostramos la notificación local.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1) Verificar si trae datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data Message recibido: ${remoteMessage.data}")
            val data = remoteMessage.data

            // Extrae cada clave del Data Payload
            val titulo       = data["titulo"]       ?: "Nuevo mensaje"
            val cuerpo       = data["cuerpo"]       ?: ""
            val nombreEmisor = data["nombreEmisor"] ?: ""
            val chatId       = data["chatId"]       ?: ""
            val uidEmisor    = data["uidEmisor"]    ?: ""

            // Mostrar notificación
            mostrarNotificacion(titulo, cuerpo, chatId, uidEmisor, nombreEmisor)
        }

        // 2) Si viniera además .notification (Notification Payload),
        //    podrías manejarlo aquí también (aunque no es nuestro caso principal).
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Message recibido: ${it.title}/${it.body}")
        }
    }

    /**
     * Construye y publica la notificación en el canal "canal_chat_mensajes".
     * Cuando el usuario haga tap, abre ChatActivity pasándole extras: chatId, uidEmisor, nombreEmisor.
     */
    private fun mostrarNotificacion(
        title: String,
        body: String,
        chatId: String,
        uidEmisor: String,
        nombreEmisor: String
    ) {
        // 1) Crear Intent para abrir ChatActivity con los extras necesarios
        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chatId", chatId)
            putExtra("uidEmisor", uidEmisor)
            putExtra("nombreEmisor", nombreEmisor)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 2) Crear canal (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombreCanal = "Chat Messages"
            val descripcion = "Notificaciones de nuevos mensajes de chat"
            val importancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel(CHANNEL_ID, nombreCanal, importancia).apply {
                this.description = descripcion
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }

        // 3) Construir la notificación
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mensaje)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // 4) Publicar la notificación con un ID único por chatId
        val notifId = chatId.hashCode().let { it and 0x00FFFFFF } // simple hash
        NotificationManagerCompat.from(this).notify(notifId, builder.build())
    }
}
