package com.example.campusgo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.data.models.Chat
import com.example.campusgo.data.repository.ManejadorImagenesAPI

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
        private val imgFotoPerfil: ImageView = itemView.findViewById(R.id.imgFotoPerfil)

        fun bind(chat: Chat) {
            txtNombreUsuario.text = chat.nombreUsuario
            txtUltimoMensaje.text = chat.ultimoMensaje

            // ✅ Usar ManejadorImagenesAPI para mostrar imagen
            ManejadorImagenesAPI.mostrarImagenDesdeUrl(
                url = chat.urlFotoPerfil,
                imageView = imgFotoPerfil,
                context = itemView.context,
                placeholderRes = R.drawable.ic_profile,
                errorRes = R.drawable.ic_profile
            )

            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onChatClick(chat)
                }
            }
        }
    }
}
