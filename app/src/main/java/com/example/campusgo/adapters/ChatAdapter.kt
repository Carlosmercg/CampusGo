package com.example.campusgo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.models.Chat

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

        fun bind(chat: Chat, onChatClick: (Chat) -> Unit) {
            txtNombreUsuario.text = chat.nombreUsuario
            txtUltimoMensaje.text = chat.ultimoMensaje
            itemView.setOnClickListener { onChatClick(chat) }
        }
    }
}
