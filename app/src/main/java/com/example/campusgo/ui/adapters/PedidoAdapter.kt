package com.example.campusgo.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusgo.data.models.Pedido
import com.example.campusgo.databinding.ItemPedidoBinding

class PedidoAdapter(
    private var pedidos: MutableList<Pedido>,
    private val onClickListener: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    //ViewHolder definition
    class PedidoViewHolder(private val binding: ItemPedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pedido: Pedido, onClickListener: (Pedido) -> Unit) {
            binding.txtNombreProducto.text = pedido.productos.first().nombre // set the product name
            binding.txtPrecioProducto.text = pedido.productos.sumOf { it.precio }.toString() //set the sum of the prices
            binding.root.setOnClickListener { onClickListener(pedido) } // set the onclick listener for each item
        }
    }
    //methods to create and set the data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPedidoBinding.inflate(layoutInflater, parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val item = pedidos[position]
        holder.bind(item, onClickListener)
    }

    override fun getItemCount(): Int = pedidos.size

    // Method to update the data in the adapter
    fun updateData(newPedidos: List<Pedido>) {
        pedidos.clear()
        pedidos.addAll(newPedidos)
        notifyDataSetChanged()
    }
}