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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        // Guardar token en Firestore si el usuario está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val db = Firebase.firestore
            val data = mapOf("fcmToken" to token)
            db.collection("usuarios")
                .document(user.uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener { Log.d(TAG, "Token actualizado en Firestore") }
                .addOnFailureListener { e -> Log.e(TAG, "Error guardando token FCM", e) }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data Message recibido: ${remoteMessage.data}")
            val data = remoteMessage.data

            val titulo       = data["titulo"]       ?: "Nuevo mensaje"
            val cuerpo       = data["cuerpo"]       ?: ""
            val nombreEmisor = data["nombreEmisor"] ?: ""
            val chatId       = data["chatId"]       ?: ""
            val uidEmisor    = data["uidEmisor"]    ?: ""

            mostrarNotificacion(titulo, cuerpo, chatId, uidEmisor, nombreEmisor)
        }
    }

    private fun mostrarNotificacion(
        title: String,
        body: String,
        chatId: String,
        uidEmisor: String,
        nombreEmisor: String
    ) {
        // 1) Intent para abrir ChatActivity con los extras esperados
        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chatId", chatId)
            putExtra("uidReceptor", uidEmisor)       // Aquí pasamos “uidReceptor” = quien nos envió el mensaje
            putExtra("nombreUsuario", nombreEmisor)  // Para mostrar el nombre en el Toolbar de ChatActivity
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 2) Crear (si hace falta) el canal de notificación en Android 8+

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombreCanal = "Chat Messages"
            val descripcion = "Notificaciones de nuevos mensajes de chat"
            val importancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel(CHANNEL_ID, nombreCanal, importancia).apply {
                description = descripcion
            }
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

        val notifId = chatId.hashCode().let { id -> id and 0x00FFFFFF }
        NotificationManagerCompat.from(this).notify(notifId, builder.build())
    }
}
