package com.example.appteste.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.appteste.R
import com.example.appteste.extensions.PARAMETRO_PRECO
import com.example.appteste.model.Produtos

class ListViewProdutoAdapter(val context: Context, val produtos: List<Produtos>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {


        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflater.inflate(R.layout.adapter_lista, null)

        var fantasia = view.findViewById<TextView>(R.id.id_descproduto)
        var codigo = view.findViewById<TextView>(R.id.id_quantidade)
        var razao = view.findViewById<TextView>(R.id.id_unidade)
        var limite = view.findViewById<TextView>(R.id.id_preco)



        //razao.text = jogos.get(position).Razao
        //codigo.text = jogos.get(position).Codigo.toString()
        fantasia.text = produtos.get(position).proDesc
        codigo.text = produtos.get(position).codigo.toString()
        razao.text = "${produtos.get(position).unidade} /  ${produtos.get(position).qtdeUnidade}"
        limite.text = "Estoque: ${produtos.get(position).estoqueDisponivel}"

        if(PARAMETRO_PRECO == 1) {
            var preco = view.findViewById<TextView>(R.id.id_total)
            preco.setVisibility(View.VISIBLE)
            preco.text = "Preço: R$ ${produtos.get(position).precoVenda}"
        }
        return view;
    }

    override fun getCount(): Int {
        return produtos.size
    }

    override fun getItem(position: Int): Any {
        return produtos.get(position);
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

}