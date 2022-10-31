package com.example.appteste.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.appteste.R
import com.example.appteste.database.ItensPedido


class CarrinhoAdapter(val context:Context, val clientes: List<ItensPedido>): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflater.inflate(R.layout.adapter_carrinho, null)

        var produto = view.findViewById<TextView>(R.id.id_descproduto)
        var qtd = view.findViewById<TextView>(R.id.id_quantidade)
        var unidade = view.findViewById<TextView>(R.id.id_unidade)
        var preco = view.findViewById<TextView>(R.id.id_preco)
        var total = view.findViewById<TextView>(R.id.id_total)

        produto.text = clientes.get(position).prodesc
        qtd.text = "QTD: ${clientes.get(position).quantidade.toString()}"
        unidade.text = "${clientes.get(position).unpunidade}/${clientes.get(position).unpquant}"
        preco.text = "Preço: R$${clientes.get(position).precovenda}"
        total.text = "Total: R$ ${clientes.get(position).quantidade * clientes.get(position).precovenda}"
        return view;

    }

    override fun getCount(): Int {
            return clientes.size
    }

    override fun getItem(position: Int): Any {
        return clientes.get(position);
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }
}