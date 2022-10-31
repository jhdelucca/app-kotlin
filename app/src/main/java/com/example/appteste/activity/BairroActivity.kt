package com.example.appteste.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appteste.api.Endpoint
import com.example.appteste.databinding.ActivityBairroBinding
import com.example.appteste.model.Bairro
import com.example.appteste.util.NetworkUtils
import com.google.gson.Gson
//import com.example.appteste.databinding.ActivityBairroBinding
import kotlinx.android.synthetic.main.activity_bairro.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BairroActivity : AppCompatActivity() {

    lateinit var binding: ActivityBairroBinding

    val path = "http://192.168.0.107:8080/"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBairroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bairro = intent.extras?.getParcelable<Bairro>("bairro")
        if (bairro != null) {
            binding.idCodigoBairro.text = "${bairro.codigo}"
            binding.idDescricaoBairro.text = "${bairro.descricao}"
        }

        binding.btnPost.setOnClickListener {
            Inserir()
        }
    }

    fun Inserir() {
        val retrofitClient = NetworkUtils.getRetrofitInstance(path)
        val endpoint = retrofitClient.create(Endpoint::class.java)

        val bairro = Bairro(id_codigoBairro.text.toString().toInt(),id_descricaoBairro.text.toString() , 1)
        val body: RequestBody = Gson().toJson(bairro).toRequestBody("application/json".toMediaType())

        val callback = endpoint.postBairro(body)

        callback.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var data = response.body()?.string()

                if (data == null) {
                    data = response.errorBody()?.string()
                }
                val json = JSONObject(data!!)

                if(response.isSuccessful) {
                    Toast.makeText(baseContext, "Bairro ${bairro.descricao} cadastrado", Toast.LENGTH_LONG).show()

                }else{
                    Toast.makeText(baseContext, "${json.getString("error")}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(baseContext, "Errado!!!!", Toast.LENGTH_LONG).show()
            }
        })
    }
}