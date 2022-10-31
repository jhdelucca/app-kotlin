package com.example.appteste.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.appteste.R
import com.example.appteste.model.Cliente


class ListaAdapter(val context:Context , val clientes: List<Cliente>): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflater.inflate(R.layout.adapter_lista, null)

        var fantasia = view.findViewById<TextView>(R.id.id_descproduto)
        var codigo = view.findViewById<TextView>(R.id.id_quantidade)
        var razao = view.findViewById<TextView>(R.id.id_unidade)
        var limite = view.findViewById<TextView>(R.id.id_preco)

         //razao.text = jogos.get(position).Razao
         //codigo.text = jogos.get(position).Codigo.toString()
        fantasia.text = clientes.get(position).fantasia
        codigo.text = clientes.get(position).codigo.toString()
        razao.text = clientes.get(position).razao
        limite.text = "Limite: R$${clientes.get(position).limite}"
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