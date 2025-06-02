package com.example.campusgo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.R
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.data.repository.ManejadorImagenesAPI

class UsuarioAdapter(
    private val usuarios: List<Usuario>,
    private val onChatearClick: (Usuario) -> Unit,
    private val onPerfilClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(usuarios[position])
    }

    override fun getItemCount(): Int = usuarios.size

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombreUsuario: TextView = itemView.findViewById(R.id.txtNombreUsuario)
        private val txtCarrera: TextView = itemView.findViewById(R.id.txtCarreraUsuario)
        private val txtUniversidad: TextView = itemView.findViewById(R.id.txtUniversidadUsuario)
        private val imgFotoPerfil: ImageView = itemView.findViewById(R.id.imgPerfilUsuario)
        private val btnChatear: Button = itemView.findViewById(R.id.btnChatear)
        private val btnPerfil: Button = itemView.findViewById(R.id.btnPerfil)

        fun bind(usuario: Usuario) {
            txtNombreUsuario.text = "${usuario.nombre} ${usuario.apellido}"
            txtCarrera.text = usuario.carrera
            txtUniversidad.text = usuario.universidad

            // ✅ Usar ManejadorImagenesAPI para mostrar foto de perfil
            ManejadorImagenesAPI.mostrarImagenDesdeUrl(
                url = usuario.urlFotoPerfil,
                imageView = imgFotoPerfil,
                context = itemView.context,
                placeholderRes = R.drawable.ic_profile,
                errorRes = R.drawable.ic_profile
            )

            btnChatear.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onChatearClick(usuario)
                }
            }

            btnPerfil.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onPerfilClick(usuario)
                }
            }
        }
    }
}
