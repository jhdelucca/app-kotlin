package com.example.appteste.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appteste.database.AppDataBase
import com.example.appteste.database.Configuracao
import com.example.appteste.databinding.ActivityConfigBinding
import com.example.appteste.extensions.URL_PRINCIPAL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigBinding
    private lateinit var appDataBase: AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDataBase = AppDataBase.getDatabase(this)

        findByConfig()

        binding.buttonLogin.setOnClickListener {
            delete()
            insertDaoConfig()
            finish()
        }
    }

    private suspend fun recebeValorFuncao(recebeValor: Configuracao) {
        withContext(Dispatchers.Main) {
            if(recebeValor != null) {
                binding.host.setText(recebeValor.host)
                binding.porta.setText(recebeValor.porta.toString())
                if(recebeValor.seguranca) {
                    binding.https.setChecked(true)
                }
            }
        }
    }

    private fun findByConfig() {
        GlobalScope.launch(Dispatchers.IO) {
            val recebeValor = appDataBase.configDao().findByAll()
            recebeValorFuncao(recebeValor)
        }
    }

    private fun insertDaoConfig() {

        val host = binding.host.text.toString()
        val porta = binding.porta.text.toString().toInt()
        val seguranca= binding.https.isChecked


        if (host.isNotEmpty()) {
            val config = Configuracao(host, porta, seguranca)

            GlobalScope.launch(Dispatchers.IO) {
                appDataBase.configDao().insert(config)
                URL_PRINCIPAL = "http://${config.host}:${config.porta}/"
            }
            Toast.makeText(this, "Gravado com Sucesso", Toast.LENGTH_LONG).show()
        }
    }

    private fun delete() {
        GlobalScope.launch(Dispatchers.IO) {
        appDataBase.configDao().delete()
        }
    }
}