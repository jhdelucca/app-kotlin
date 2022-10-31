package com.example.appteste.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.appteste.R
import com.example.appteste.model.Pedidos

class ListaPedidosAdapter(val context:Context, val pedidos: List<Pedidos>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {


        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflater.inflate(R.layout.adapter_lista, null)

        var fantasia = view.findViewById<TextView>(R.id.id_descproduto)
        var codigo = view.findViewById<TextView>(R.id.id_quantidade)
        var razao = view.findViewById<TextView>(R.id.id_unidade)
        var limite = view.findViewById<TextView>(R.id.id_preco)
        var card:CardView = view.findViewById<CardView>(R.id.card)

         //razao.text = jogos.get(position).Razao
         //codigo.text = jogos.get(position).Codigo.toString()
        fantasia.text = "${pedidos.get(position).serie} - ${pedidos.get(position).numero}"
        codigo.text = pedidos.get(position).dtEmissao
        razao.text = pedidos.get(position).razao
        limite.text = "R$ ${pedidos.get(position).valor}"

        return view;

    }

    override fun getCount(): Int {
            return pedidos.size
    }

    override fun getItem(position: Int): Any {
        return pedidos.get(position);
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }




}