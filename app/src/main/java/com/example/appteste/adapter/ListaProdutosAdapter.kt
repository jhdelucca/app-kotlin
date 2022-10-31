package com.example.appteste.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appteste.R
import com.example.appteste.model.Produtos

class ListaProdutosAdapter(val context: Context , val produtos:List<Produtos>) : RecyclerView.Adapter<ListaProdutosAdapter.ProdutoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val itemLista = LayoutInflater.from(context).inflate(R.layout.adapter_lista, parent, false)
        val holder = ProdutoViewHolder(itemLista)
        return holder;
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        holder.codigo.text = "Codigo: ${produtos.get(position).codigo}"
        holder.qtdeUnidade.text = "${produtos.get(position).unidade} / ${produtos.get(position).qtdeUnidade}"
        holder.descricao.text = "${produtos.get(position).proDesc}"
        holder.estoque.text = "${produtos.get(position).estoqueDisponivel}"
    }

    override fun getItemCount() = produtos.size

    inner class ProdutoViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val codigo = itemView.findViewById<TextView>(R.id.id_quantidade)
        val qtdeUnidade = itemView.findViewById<TextView>(R.id.id_unidade)
        val descricao = itemView.findViewById<TextView>(R.id.id_descproduto)
        val estoque = itemView.findViewById<TextView>(R.id.id_preco)
    }

}