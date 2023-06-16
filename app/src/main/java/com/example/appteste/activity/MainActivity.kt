package com.example.appteste.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.example.appteste.R
import com.example.appteste.api.Endpoint
import com.example.appteste.database.AppDataBase
import com.example.appteste.extensions.*
import com.example.appteste.model.Filial
import com.example.appteste.model.User
import com.example.appteste.util.preferences.SharedPreferences
import com.example.appteste.request.UserRequest
import com.example.appteste.util.NetworkUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    lateinit var btn2: Button;
    lateinit var campo: EditText;
    lateinit var senha:EditText;
    lateinit var spinner: Spinner
    var listaFilial: List<Filial> = listOf()
    private lateinit var appDataBase: AppDataBase



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn2 = findViewById(R.id.buttonLoga2)
        campo = findViewById(R.id.editTextUsr);
        senha = findViewById(R.id.editTextSenha);
        spinner = findViewById(R.id.spiFilial)
        appDataBase = AppDataBase.getDatabase(this)

        val singToolbar:Toolbar = findViewById(R.id.navbar)
        setSupportActionBar(singToolbar)
        supportActionBar?.title = null

        GetURL()
        campo.requestFocus()

        campo.setOnFocusChangeListener { v, hasFocus ->
            if(URL_PRINCIPAL.isNotEmpty()) {
                getFiliais()
            }
        }

       /** campo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                getFiliais()
            }

        })**/



        /**  btn.setOnClickListener(View.OnClickListener{
            //var texto = campo.text.toString();
           // Toast.makeText(applicationContext,texto,Toast.LENGTH_LONG);
            val user = User(campo.text.toString(),senha.text.toString() , 1)
            val it = Intent(applicationContext,TesteActivity::class.java)
          val params = Bundle()
            params.putString("texto" , campo.text.toString())
            params.putString("senha" , senha.text.toString())
            params.putString("filial" , spinner.selectedItem.toString())
            it.putExtras(params) // Tipo Bundle
            it.putExtra("user", user) // Tipo Parcelable
            startActivity(it)
        })**/

        btn2.setOnClickListener {
            if(URL_PRINCIPAL.isNotEmpty()) {
                getColaborador()
            }else{
                Toast.makeText(applicationContext, "Preencha as configurações" , Toast.LENGTH_LONG).show()
            }
        }
    }

    fun GetURL() {
        GlobalScope.launch(Dispatchers.IO) {
            val recebeValor = appDataBase.configDao().findByAll()
            if(recebeValor != null) {
                URL_PRINCIPAL = "http://${recebeValor.host}:${recebeValor.porta}/"
            }

        }
    }

   fun getFiliais() {
        val retrofitCliente = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL)
        val endpoint = retrofitCliente.create(Endpoint::class.java)
        val usuario = campo.text.toString()
        val callback = endpoint.getFiliais(usuario)

       callback.enqueue(object : Callback<ResponseBody> {
           override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
               var data = response.body()?.string()

               if (data == null) {
                   data = response.errorBody()?.string()
               }

               if(response.isSuccessful) {
                   val json = JSONArray(data!!)
                   listaFilial = GsonBuilder().create().fromJson(json.toString(), Array<Filial>::class.java).toList()
                   if(listaFilial.isNotEmpty()) {
                       spinner.adapter = ArrayAdapter(applicationContext, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item , listaFilial)
                       spinner.setSelection(0)
                   }else{
                      Toast.makeText(baseContext, "Verifique Usuario", Toast.LENGTH_LONG).show()
                   }
               }else{
                   Toast.makeText(baseContext, JSONObject(data!!).getString("message"), Toast.LENGTH_LONG).show()
               }
           }

           override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               Toast.makeText(baseContext, "Erro de Serviço, verifique os dados de conexão", Toast.LENGTH_LONG).show()
           }
       })

    }

    fun getColaborador() {
        val retrofitCliente = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL);
        val endpoint = retrofitCliente.create(Endpoint::class.java)
        val usuario = campo.text.toString()
        val callback = endpoint.getColaborador(usuario)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                   DADOS_USUARIO = json.getJSONObject("dados")
                   Login()
                }else{Toast.makeText(baseContext, json.getString("message"), Toast.LENGTH_LONG).show()

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Erro de Serviço!!", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun Login() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("http://192.168.0.13:8080/vendasguardian/")
        val endpoint = retrofitClient.create(Endpoint::class.java)

        val filial = spinner.adapter.getItem(spinner.selectedItemPosition) as Filial
        val user = User(campo.text.toString(),senha.text.toString(),filial.codigo)

        val body: RequestBody = Gson().toJson(user).toRequestBody("application/json".toMediaType())
        val callback = endpoint.authenticate(body)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                 data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                    val it = Intent(applicationContext, TesteActivity::class.java)
                    val userReq = UserRequest(json.getString("token") , json.getString("usuarioNome"), json.getString("login"), json.getInt("filialCodigo"))
                    TOKEN = userReq.token
                    SharedPreferences(applicationContext).storeString("token" , userReq.token)
                    NOME_USUARIO = userReq.usuarioNome
                    FILIAL = filial.codigo
                    it.putExtra("user", userReq) // Tipo Parcelable
                    campo.setText("")
                    senha.setText("")
                    campo.requestFocus()
                    getParametros()
                    startActivity(it)
                    finish()
                }else{
                    Toast.makeText(baseContext, "${json.getString("mensagemUsuario")}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun getParametros() {
        val retrofitClient = NetworkUtils.getRetrofitInstance(URL_PRINCIPAL);
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.getParametros(FILIAL)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                    PARAMETRO_PRECO = json.getInt("exibePreco")
                  
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext,"Parametors Não Selecionados" , Toast.LENGTH_SHORT )
            }

        })
    }

     override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val inflater:MenuInflater = menuInflater
    inflater.inflate(R.menu.menu_teste,menu)

    return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle item selection
    return when (item.itemId) {
    R.id.menuConfig -> {
        val intent = Intent(this, ConfigActivity::class.java)
        startActivity(intent)
        true
    true
    }
    R.id.menuHelp -> {
        startActivity(Intent(applicationContext,CanhotoActivity::class.java))
    true
    }
    else -> super.onOptionsItemSelected(item)
    }
    }

}