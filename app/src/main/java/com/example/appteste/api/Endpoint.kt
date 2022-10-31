package com.example.appteste.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap


interface Endpoint {

    @POST("autenticacao/entrar")
    fun authenticate(@Body body: RequestBody) : Call<ResponseBody>

    @POST("pedido")
    fun inserePedidos(@Header("Authorization") authorization:String, @Body body: RequestBody) : Call<ResponseBody>

    @POST("pedido/alterar")
    fun alteraPedidos(@Header("Authorization") authorization:String, @Body body: RequestBody) : Call<ResponseBody>

    @GET("appcliente.rule")
    fun listCliente(@QueryMap options: Map<String,String>) : Call<ResponseBody>

    @GET("pessoa")
    fun listClienteKotlin() : Call<ResponseBody>

    @GET("pessoa/{codigo}")
    fun listClienteCodigo(@Path("codigo") codigo:Int) : Call<ResponseBody>

    @POST("bairro")
    fun postBairro(@Body body:RequestBody):Call<ResponseBody>

    @GET("colaborador/{usuario}")
    fun getColaborador(@Path("usuario") usuario:String) : Call<ResponseBody>

    @GET("colaborador/{usuario}/autorizar")
    fun getColaboradorAutorizar(@Path("usuario") usuario:String) : Call<ResponseBody>

    @GET("cliente")
    fun getClientes(@Query("usuario") usuario:String = ""):Call<ResponseBody>

    @POST("pedidos")
    fun getPedidos(@Body body: RequestBody):Call<ResponseBody>

    @POST("pedidos/unico")
    fun getPedidosUnico(@Body body: RequestBody):Call<ResponseBody>

    @GET("filial/{usuario}")
    fun getFiliais(@Path("usuario")usuario:String):Call<ResponseBody>

    @GET("produtos")
    fun getProdutos(@Query("filial") filial:Int):Call<ResponseBody>

    @GET("parametros")
    fun getParametros(@Query("filial") filial: Int): Call<ResponseBody>

    @POST("produtos/unidade")
    fun getUnidadePro(@Body body:RequestBody):Call<ResponseBody>

    @GET("natoper")
    fun getNatureza(@Query("natureza") natureza:String = "" , @Query("filial") filial: Int) : Call<ResponseBody>

    @GET("formapagto")
    fun getForma(@Query("codigocliente") codigocliente:Int , @Query("formapagto") formapagto:String = "") : Call<ResponseBody>

    @GET("condpagto")
    fun getCondicao(@Query("codigocliente") codigocliente:Int , @Query("condpagto") condpagto:Int = 0) : Call<ResponseBody>
}