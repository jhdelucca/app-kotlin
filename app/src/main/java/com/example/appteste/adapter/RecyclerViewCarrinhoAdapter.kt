package com.example.appteste.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.appteste.R
import com.example.appteste.database.ItensPedido

class RecyclerViewCarrinhoAdapter(val context: Context, val itens:List<ItensPedido> , var clickBtns: ClickBtns) : RecyclerView.Adapter<RecyclerViewCarrinhoAdapter.CarrinhoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        var itemLista = LayoutInflater.from(context).inflate(R.layout.adapter_carrinho, parent, false)
        val holder = CarrinhoViewHolder(itemLista)
        return holder;
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        holder.produto.text = itens.get(position).prodesc
        holder.qtd.text = "QTD: ${itens.get(position).quantidade}"
        holder.unidade.text = "${itens.get(position).unpunidade}/${itens.get(position).unpquant}"
        holder.preco.text = "Preço: R$${itens.get(position).precovenda}"
        holder.total.text = "Total: R$ ${itens.get(position).quantidade * itens.get(position).precovenda}"

        holder.btnEdit.setOnClickListener(View.OnClickListener {
            clickBtns.editItem(itens.get(position))
        })

        holder.btnExc.setOnClickListener(View.OnClickListener {
            clickBtns.excluirItens(itens.get(position))

            notifyItemRemoved(position)

            notifyDataSetChanged()
        })
    }

    interface ClickBtns {

        fun editItem(itensPedido: ItensPedido)

        fun excluirItens(itensPedido: ItensPedido)

    }

    override fun getItemCount() = itens.size

    inner class CarrinhoViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var produto = itemView.findViewById<TextView>(R.id.id_descproduto)
        var qtd = itemView.findViewById<TextView>(R.id.id_quantidade)
        var unidade = itemView.findViewById<TextView>(R.id.id_unidade)
        var preco = itemView.findViewById<TextView>(R.id.id_preco)
        var total = itemView.findViewById<TextView>(R.id.id_total)
        var btnEdit = itemView.findViewById<AppCompatImageView>(R.id.btnEdit)
        var btnExc =  itemView.findViewById<AppCompatImageView>(R.id.btnExc)
    }

}