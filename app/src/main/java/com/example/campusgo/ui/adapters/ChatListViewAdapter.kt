package com.example.campusgo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat

class ChatListViewAdapter(
    private val chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListViewAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int = chats.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombreUsuario: TextView = itemView.findViewById(R.id.txtNombreUsuario)
        private val txtUltimoMensaje: TextView = itemView.findViewById(R.id.txtUltimoMensaje)

        fun bind(chat: Chat) {
            txtNombreUsuario.text = chat.nombreUsuario
            txtUltimoMensaje.text = chat.ultimoMensaje

            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChatClick(chat)
                }
            }
        }
    }
}
