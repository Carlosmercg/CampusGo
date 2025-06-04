package com.example.campusgo.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.campusgo.R
import com.example.campusgo.ui.chat.ChatActivity
import com.example.campusgo.ui.mapas.MapaCompradorActivity
import com.example.campusgo.ui.venta.VentaActivity
import com.google.firebase.auth.FirebaseAuth
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
                .set(data)
                .addOnSuccessListener { Log.d(TAG, "Token actualizado en Firestore") }
                .addOnFailureListener { e -> Log.e(TAG, "Error guardando token FCM", e) }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data Message recibido: ${remoteMessage.data}")
            val dataMap = remoteMessage.data
            val tipo         = dataMap["tipo"] ?: ""
            val titulo       = dataMap["titulo"] ?: ""
            val cuerpo       = dataMap["body"]   ?: ""
            val chatId       = dataMap["chatId"] ?: ""
            val productoId   = dataMap["productoId"] ?: ""
            val uidEmisor    = dataMap["uidEmisor"] ?: ""
            val uidComprador = dataMap["uidComprador"] ?: ""
            val uidVendedor  = dataMap["uidVendedor"] ?: ""
            val nombreEmisor = dataMap["nombreEmisor"] ?: ""
            val nombreCompr  = dataMap["nombreComprador"] ?: ""
            val nombreVend   = dataMap["nombreVendedor"] ?: ""

            when (tipo) {
                "chat" -> {
                    mostrarNotificacionChat(titulo, cuerpo, chatId, uidEmisor, nombreEmisor)
                }
                "nuevaCompra" -> {
                    mostrarNotificacionNuevaCompra(
                        titulo,
                        cuerpo,
                        chatId,
                        uidComprador,
                        nombreCompr,
                        productoId
                    )
                }
                "compraAceptada" -> {
                    mostrarNotificacionCompraAceptada(
                        titulo,
                        cuerpo,
                        chatId,
                        productoId
                    )
                }
                "compraRechazada" -> {
                    mostrarNotificacionCompraRechazada(
                        titulo,
                        cuerpo,
                        chatId,
                        productoId,
                        uidVendedor,
                        nombreVend
                    )
                }
                else -> {
                    mostrarNotificacionChat(titulo, cuerpo, chatId, uidEmisor, nombreEmisor)
                }
            }
        }
    }

    // ------------------------------------------------------------
    // 1) Notificación de chat (sin cambios)
    // ------------------------------------------------------------
    private fun mostrarNotificacionChat(
        title: String,
        body: String,
        chatId: String,
        uidEmisor: String,
        nombreEmisor: String
    ) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chatId", chatId)
            putExtra("uidReceptor", uidEmisor)
            putExtra("nombreUsuario", nombreEmisor)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        publicar(title, body, pendingIntent, chatId)
    }

    // ------------------------------------------------------------
    // 2) Notificación "nuevaCompra" para vendedor
    // ------------------------------------------------------------
    private fun mostrarNotificacionNuevaCompra(
        title: String,
        body: String,
        chatId: String,
        uidComprador: String,
        nombreComprador: String,
        productoId: String
    ) {
        // =========================
        //     SECCIÓN MODIFICADA
        // =========================
        val intent = Intent(this, VentaActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Le cambiamos la clave para que VentaActivity consiga el "pedidoID"
            putExtra("pedidoID", chatId)     // NUEVO: enviamos pedidoID correctamente
            putExtra("uidComprador", uidComprador)
            putExtra("productoId", productoId)
            putExtra("nombreUsuario", nombreComprador)
        }
        // FIN DE NUEVO

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        publicar(title, body, pendingIntent, chatId)
    }

    // ------------------------------------------------------------
    // 3) Notificación "compraAceptada" para comprador (abre MapaComprador)
    // ------------------------------------------------------------
    private fun mostrarNotificacionCompraAceptada(
        title: String,
        body: String,
        chatId: String,
        productoId: String
    ) {
        val intent = Intent(this, MapaCompradorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chatId", chatId)
            putExtra("productoId", productoId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        publicar(title, body, pendingIntent, chatId)
    }

    // ------------------------------------------------------------
    // 4) Notificación "compraRechazada" para comprador (abre ChatActivity)
    // ------------------------------------------------------------
    private fun mostrarNotificacionCompraRechazada(
        title: String,
        body: String,
        chatId: String,
        productoId: String,
        uidVendedor: String,
        nombreVendedor: String
    ) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chatId", chatId)
            putExtra("uidReceptor", uidVendedor)
            putExtra("nombreUsuario", nombreVendedor)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        publicar(title, body, pendingIntent, chatId)
    }

    // ------------------------------------------------------------
    // 5) Función genérica que crea canal y publica notificación (sin cambios excepto BigTextStyle)
    // ------------------------------------------------------------
    private fun publicar(
        title: String,
        body: String,
        pendingIntent: PendingIntent,
        chatId: String
    ) {
        // 5.1) Crear canal si hace falta (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombreCanal = "Chat & Compras"
            val descripcion = "Notificaciones de chat y compras"
            val importancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel(CHANNEL_ID, nombreCanal, importancia).apply {
                this.description = descripcion
            }
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }

        // 5.2) Construir el builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mensaje)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // NUEVO: Utilizar BigTextStyle para la descripción completa
        builder.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(body)
        )
        // FIN DE NUEVO

        // 5.3) CHEQUEO DE PERMISO POST_NOTIFICATIONS en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permiso != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "No se tiene permiso POST_NOTIFICATIONS; omitiendo notify()")
                return
            }
        }

        // 5.4) Si llegamos aquí, tenemos permiso (o Android < 13), entonces publicamos
        val notifId = chatId.hashCode().and(0x00FFFFFF)
        NotificationManagerCompat.from(this).notify(notifId, builder.build())
    }
}
