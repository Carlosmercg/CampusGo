package com.example.campusgo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.models.Chat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position], onChatClick)
    }

    override fun getItemCount(): Int = chats.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombreUsuario: TextView = itemView.findViewById(R.id.txtNombreUsuario)
        private val txtUltimoMensaje: TextView = itemView.findViewById(R.id.txtUltimoMensaje)
        private val imgUsuarioChat: ImageView = itemView.findViewById(R.id.imgUsuarioChat)
        private val checkImageView: ImageView = itemView.findViewById(R.id.checkImageView)

        fun bind(chat: Chat, onChatClick: (Chat) -> Unit) {
            // Configurar elementos básicos
            txtNombreUsuario.text = chat.nombreUsuario
            txtUltimoMensaje.text = chat.ultimoMensaje

            // Mostrar imagen de perfil
            imgUsuarioChat.visibility = View.VISIBLE
            imgUsuarioChat.setImageResource(R.drawable.ic_profile)

            // Configurar check (ejemplo básico)
            checkImageView.visibility = View.VISIBLE
            checkImageView.setImageResource(R.drawable.check)

            itemView.setOnClickListener { onChatClick(chat) }
            txtUltimoMensaje.text = "${chat.ultimoMensaje} • ${formatearTimestamp(chat.timestamp)}"
        }
    }
    private fun formatearTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}