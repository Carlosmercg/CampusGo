// CampusGo/app/src/main/java/com/example/campusgo/services/NotificacionesChatService.kt
package com.example.campusgo.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.campusgo.R
import com.example.campusgo.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificacionesChatService : Service() {

    private val TAG = "NotisChatService"
    private lateinit var notisRef: DatabaseReference
    private var childListener: ChildEventListener? = null

    override fun onCreate() {
        super.onCreate()

        val uidActual = FirebaseAuth.getInstance().currentUser?.uid
        if (uidActual == null) {
            Log.w(TAG, "No hay usuario autenticado, deteniendo servicio")
            stopSelf()
            return
        }

        // 1) Referencia al nodo /notificacionesChat/{uidActual}
        notisRef = FirebaseDatabase.getInstance()
            .getReference("notificacionesChat")
            .child(uidActual)

        // 2) Crear el ChildEventListener
        childListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Cada vez que aparece un nuevo child bajo /notificacionesChat/{uidActual}
                val data = snapshot.value as? Map<*, *> ?: return

                // Convertir a Map<String, String> (o <String, Any>)
                val mapData = data.entries.associate { it.key.toString() to it.value.toString() }

                // Verificar que sea notificación de “chat”
                if (mapData["tipo"] == "chat") {
                    enviarNotificacionChat(mapData)
                }

                // Opcional: borrar el nodo de notificación después de mostrarla
                snapshot.ref.removeValue()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error escuchando notificacionesChat", error.toException())
            }
        }

        // 3) Adjuntar el listener
        notisRef.addChildEventListener(childListener as ChildEventListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // No es un servicio “bindable”
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remover el listener para no filtrar memoria
        childListener?.let {
            notisRef.removeEventListener(it)
        }
    }

    /**
     * Envía una notificación local de chat, recibiendo los campos en mapData:
     * mapData["title"], mapData["body"], mapData["chatId"], mapData["emisorId"]…
     */
    private fun enviarNotificacionChat(mapData: Map<String, String>) {
        val title     = mapData["title"] ?: "Mensaje nuevo"
        val body      = mapData["body"]  ?: ""
        val chatId    = mapData["chatId"]
        val emisorId  = mapData["emisorId"]

        // 1) Intent para abrir ChatActivity
        val intent = Intent(this, ChatActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("chatId", chatId)
            putExtra("uidReceptor", FirebaseAuth.getInstance().currentUser?.uid)
            putExtra("uidEmisor", emisorId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2) Crear canal de notificación para “chat” (Android O+)
        val channelId = "CANAL_CHAT"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Notificaciones de Chat"
            val channelDescription = "Recibes notificaciones cuando te envían mensajes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // 3) Construir la notificación
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.gfg)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // 4) Mostrarla con ID único (por ejemplo hash de chatId)
        val notifId = chatId?.hashCode()?.let { it % 1000 } ?: 0
        with(NotificationManagerCompat.from(this)) {
            notify(notifId, builder.build())
        }
    }
}
