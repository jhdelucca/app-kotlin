package com.example.appteste.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.example.appteste.adapter.ListViewProdutoAdapter
import com.example.appteste.R
import com.example.appteste.api.Endpoint
import com.example.appteste.database.AppDataBase
import com.example.appteste.database.ItensPedido
import com.example.appteste.databinding.ActivityProdutoBinding
import com.example.appteste.extensions.*
import com.example.appteste.model.Produtos
import com.example.appteste.model.UnidadePro
import com.example.appteste.request.UnidadeProRequest
import com.example.appteste.util.NetworkUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_produto.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

@OptIn(DelicateCoroutinesApi::class)
class ProdutoActivity() : AppCompatActivity() {

    lateinit var binding: ActivityProdutoBinding
    var listaProdutos : List<Produtos> = listOf()
    var listUnidadePro: List<UnidadePro> = listOf()
    var listaPesquisaProduto: List<Produtos> = listOf()
    private lateinit var appDataBase: AppDataBase
    var precoTabela:Double = 0.00
    var totalPedido:Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val singToolbar: Toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(singToolbar)
        supportActionBar?.title = null

        appDataBase = AppDataBase.getDatabase(this)


      /**  binding.idListaProdutos.layoutManager = LinearLayoutManager(this)
        binding.idListaProdutos.setHasFixedSize(true) **/

        listarProdutos()
       // preencheTotal()

        binding.editTextProduto.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val textDigitado = s.toString()
                var listaTemporaria: MutableList<Produtos> = mutableListOf()

                for (produtos: Produtos in listaProdutos) {
                    if((produtos.proDesc.contains(textDigitado,true) || produtos.codigo.toString().contains(textDigitado))
                        && textDigitado != "") {
                        listaTemporaria.add(produtos)
                    }
                }
                if((listaTemporaria.isNotEmpty()) || (listaTemporaria.isEmpty() && textDigitado.isNotEmpty())) {
                    binding.idListaProdutos.adapter = ListViewProdutoAdapter(applicationContext, listaTemporaria)
                    listaPesquisaProduto = listaTemporaria

                }else{
                    binding.idListaProdutos.adapter = ListViewProdutoAdapter(applicationContext, listaProdutos)
                    listaPesquisaProduto = listOf()
                }
            }
        })

        binding.idListaProdutos.setOnItemClickListener { parent, view, position, id ->
            if(listaPesquisaProduto.size  > 0) {

                binding.editEstoquCar.setText(listaPesquisaProduto.get(position).estoqueDisponivel.toString())
                if (PARAMETRO_PRECO == 1) {
                    binding.editValorCar.setText(listaPesquisaProduto.get(position).precoVenda.toString())
                }

                binding.txtDescricao.text = listaPesquisaProduto.get(position).proDesc
                CODIGO_PRODUTO = listaPesquisaProduto.get(position).codigo
                precoTabela = listaPesquisaProduto.get(position).precoVenda
                listarUnidadePro(listaPesquisaProduto.get(position).codigo)
                binding.editQuantidadeCar.setText("1")

            }else{
                binding.editEstoquCar.setText(listaProdutos.get(position).estoqueDisponivel.toString())
                if (PARAMETRO_PRECO == 1) {
                    binding.editValorCar.setText(listaProdutos.get(position).precoVenda.toString())
                }

                binding.txtDescricao.text = listaProdutos.get(position).proDesc
                CODIGO_PRODUTO = listaProdutos.get(position).codigo
                precoTabela = listaProdutos.get(position).precoVenda
                listarUnidadePro(listaProdutos.get(position).codigo)
                binding.editQuantidadeCar.setText("2")
            }
        }

        binding.btnSalvar.setOnClickListener {
            if(editQuantidadeCar.text.isEmpty() || editValorCar.text.isEmpty()) {
                Toast.makeText(baseContext , "Quantidade ou valor em branco" , Toast.LENGTH_LONG).show()
            }else if (editEstoquCar.text.toString().toDouble() < editQuantidadeCar.text.toString().toDouble()) {
                Toast.makeText(baseContext , "Quantidae maior que estoque!!" , Toast.LENGTH_LONG).show()
            }else{
                insertDaoItensPedido()
            }
        }

        //binding.btnCancelar.setOnClickListener {
           // limparCampos()
        //}

        binding.btnCarrinho.setOnClickListener {
            startActivity(Intent(this, CarrinhoActivity::class.java))
        }

        binding.btnLixo.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("Deseja Cancelar o Pedido? O mesmo não será gravado.")
                .setNegativeButton("Não") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Sim") { dialog, which ->
                    delete()
                    STATUS = "I"
                    finish()
                }

            builder.show()
        }
    }

    override fun onResume() {
        super.onResume()
        preencheTotal()
    }

    private fun preencheTotal() {
        GlobalScope.launch(Dispatchers.IO) {
            val recebeValor = appDataBase.itenspedidoDao().somaValor()
            totalPedido = recebeValor
            setTotal()

        }
    }

    suspend fun setTotal() {
        withContext(Dispatchers.Main) {
            val format = NumberFormat.getCurrencyInstance(Locale("pt", "br"))
            binding.txtTotal.setText(format.format(totalPedido))
        }
    }

    private suspend fun somaValor(itensPedido: ItensPedido) {
        withContext(Dispatchers.Main) {
            val format = NumberFormat.getCurrencyInstance(Locale("pt", "br"))
            val recebeValor = itensPedido.precovenda * itensPedido.quantidade
            totalPedido += recebeValor
            binding.txtTotal.setText(format.format(totalPedido))
        }
    }

    private fun insertDaoItensPedido() {
        val proDesc = binding.txtDescricao.text.toString()
        val unidade = binding.spinUnidadeCar.selectedItem.toString().split("/").get(0)
        val qtdUnidade = binding.spinUnidadeCar.selectedItem.toString().split("/").get(1).toInt()
        val quantidade = binding.editQuantidadeCar.text.toString().toDouble()
        val preco = binding.editValorCar.text.toString().toDouble()

        val itensPedido = ItensPedido(procodigo = CODIGO_PRODUTO, prodesc = proDesc, unpunidade = unidade, unpquant = qtdUnidade,
        quantidade = quantidade, precovenda = preco , precotab = precoTabela , cliente = CODIGO_CLIENTE)

        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.itenspedidoDao().insert(itensPedido)
            somaValor(itensPedido)
        }

            Toast.makeText(this, "Gravado com Sucesso", Toast.LENGTH_LONG).show()
            limparCampos()
    }

    fun limparCampos() {
        binding.editEstoquCar.setText("")
        binding.spinUnidadeCar.setAdapter(null)
        binding.editQuantidadeCar.setText("")
        binding.txtDescricao.text = ""
        binding.editValorCar.setText("")
    }

    fun listarProdutos() {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitClient.create(Endpoint::class.java)

        val callback = endpoint.getProdutos(FILIAL)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
             //   val json = JSONArray(JSONObject(data!!).getJSONArray("dados"))

                if (response.isSuccessful) {
                    if(JSONObject(data).getBoolean("resposta")) {
                        listaProdutos =  GsonBuilder().create().fromJson(JSONObject(data).getJSONArray("dados").toString(), Array<Produtos>::class.java).toList()
                        binding.idListaProdutos.adapter = ListViewProdutoAdapter(applicationContext, listaProdutos)
                    }else{
                        Toast.makeText(baseContext,
                            JSONObject(data).getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                        listaProdutos = listOf()
                        binding.idListaProdutos.adapter = ListViewProdutoAdapter(applicationContext, listaProdutos)
                    }
                } else {
                    Toast.makeText(baseContext, JSONObject(data!!).getString("message"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })

    }

    fun listarUnidadePro(codigoProduto:Int) {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val unidadePro = UnidadeProRequest(FILIAL,codigoProduto)
        val body: RequestBody = Gson().toJson(unidadePro).toRequestBody("application/json".toMediaType())

        val callback = endpoint.getUnidadePro(body)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                //   val json = JSONArray(JSONObject(data!!).getJSONArray("dados"))

                if (response.isSuccessful) {
                    if(JSONObject(data!!).getBoolean("resposta")) {
                        listUnidadePro =  GsonBuilder().create().fromJson(JSONObject(data!!).getJSONArray("dados").toString(), Array<UnidadePro>::class.java).toList()
                        binding.spinUnidadeCar.adapter = ArrayAdapter(applicationContext, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item , listUnidadePro)
                       // binding.spinUnidade.setSelection(0)
                    }else{
                        Toast.makeText(baseContext,
                            JSONObject(data).getString("mensagemUsuario"), Toast.LENGTH_LONG).show()
                        listUnidadePro = listOf()
                        binding.spinUnidadeCar.adapter = ArrayAdapter(applicationContext, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item , listUnidadePro)
                    }
                } else {
                    Toast.makeText(baseContext, JSONObject(data!!).getString("message"), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Erro ao carregar Unidade", Toast.LENGTH_LONG).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun delete() {
        GlobalScope.launch(Dispatchers.IO) {
            appDataBase.itenspedidoDao().delete()
        }
        binding.txtTotal.setText("R$ 0.00")
        Toast.makeText(baseContext, "Dados Deletados", Toast.LENGTH_LONG).show()
    }
}