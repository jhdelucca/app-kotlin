package com.example.appteste.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.appteste.R
import com.example.appteste.database.ItensPedido
import com.example.appteste.model.Cliente


class ItensAutorizarAdapter(val context:Context, val itens: List<ItensPedido>): BaseAdapter() {

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflater.inflate(R.layout.adapter_lista, null)

        var fantasia = view.findViewById<TextView>(R.id.id_descproduto)
        var codigo = view.findViewById<TextView>(R.id.id_quantidade)
        var razao = view.findViewById<TextView>(R.id.id_unidade)
        var limite = view.findViewById<TextView>(R.id.id_preco)

         //razao.text = jogos.get(position).Razao
         //codigo.text = jogos.get(position).Codigo.toString()
        fantasia.text = itens.get(position).prodesc
        codigo.text = "Preço Minimo: ${itens[position].precotab}"
        razao.text = "${itens[position].unpunidade}/${itens[position].unpquant}"
        limite.text = "Preço Venda: R$${itens[position].precovenda}"
        return view;
    }

    override fun getCount(): Int {
            return itens.size
    }

    override fun getItem(position: Int): Any {
        return itens.get(position);
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }
}