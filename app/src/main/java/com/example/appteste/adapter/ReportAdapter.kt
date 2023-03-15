package com.example.appteste.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.appteste.R
import com.example.appteste.database.ItensPedido


class ReportAdapter(val context:Context, val itens: List<ItensPedido>): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflater.inflate(R.layout.report_pedido_adapter, null)

        var produto = view.findViewById<TextView>(R.id.recebe_descricao_produto)
        var qtd = view.findViewById<TextView>(R.id.recebe_quantidade_produto)
        var preco = view.findViewById<TextView>(R.id.recebe_preco_produto)


        produto.text = itens.get(position).prodesc
        qtd.text = itens.get(position).quantidade.toString()
        preco.text = itens.get(position).precovenda.toString()
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