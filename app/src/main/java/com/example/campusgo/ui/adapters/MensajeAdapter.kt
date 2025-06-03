package com.example.campusgo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.data.models.Mensaje

class MensajeAdapter(private val mensajes: List<Mensaje>) :
    RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje_enviado, parent, false)
        return MensajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        holder.bind(mensajes[position])
    }

    override fun getItemCount(): Int = mensajes.size

    class MensajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMensaje: TextView = itemView.findViewById(R.id.txtMensajeEnviado)

        fun bind(mensaje: Mensaje) {
            txtMensaje.text = mensaje.contenido
            txtMensaje.setBackgroundResource(
                if (mensaje.esEnviado) R.drawable.bg_message_sent else R.drawable.bg_message_received
            )
        }
    }
}
