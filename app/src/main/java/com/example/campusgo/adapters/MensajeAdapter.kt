package com.example.campusgo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.models.Mensaje

class MensajeAdapter(private val mensajes: List<Mensaje>) :
    RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje, parent, false)
        return MensajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        holder.bind(mensajes[position])
    }

    override fun getItemCount(): Int = mensajes.size

    class MensajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout: ConstraintLayout = itemView.findViewById(R.id.rootLayout)
        private val txtMensaje: TextView = itemView.findViewById(R.id.txtMensaje)
        private val imgUsuarioChat: ImageView = itemView.findViewById(R.id.imgUsuarioChat)
        private val checkImageView: ImageView = itemView.findViewById(R.id.checkImageView)

        fun bind(mensaje: Mensaje) {
            txtMensaje.text = mensaje.contenido

            if (mensaje.esEnviado) {
                // Mensaje enviado - alinear a la derecha
                txtMensaje.setBackgroundResource(R.drawable.bg_message_sent)
                imgUsuarioChat.visibility = View.GONE

                // Mover check a la derecha
                val constraints = ConstraintSet()
                constraints.clone(layout)
                constraints.connect(
                    R.id.checkImageView,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END
                )
                constraints.applyTo(layout)

            } else {
                // Mensaje recibido - alinear a la izquierda
                txtMensaje.setBackgroundResource(R.drawable.bg_message_received)
                imgUsuarioChat.visibility = View.VISIBLE

                // Mover check a la izquierda
                val constraints = ConstraintSet()
                constraints.clone(layout)
                constraints.connect(
                    R.id.checkImageView,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
                constraints.applyTo(layout)
            }

            // Configurar check (ejemplo básico)
            checkImageView.visibility = View.VISIBLE
            checkImageView.setImageResource(R.drawable.check)
        }
    }
}