package com.example.appteste.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ItensPedidoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(itensPedido: ItensPedido)

    @Query("SELECT * FROM ITENSPEDIDO")
    fun findByAll():MutableList<ItensPedido>

    @Query("select sum(precovenda * quantidade) as total  from itenspedido")
    fun somaValor():Double

    @Query("DELETE FROM ITENSPEDIDO")
    fun delete()

    @Query("update ITENSPEDIDO set quantidade = :quantidade , precovenda = :precoVenda\n" +
            "where procodigo = :proCodigo and unpunidade=:unidade and unpquant = :unpQuantidade")
    fun editItem(quantidade:Double , precoVenda: Double, proCodigo:Int , unidade:String , unpQuantidade:Int)

    @Query("DELETE FROM ITENSPEDIDO WHERE procodigo = :proCodigo and unpunidade=:unidade and unpquant = :unpQuantidade")
    fun deleteItemUnico(proCodigo:Int , unidade:String , unpQuantidade:Int)

    @Query("SELECT * FROM ITENSPEDIDO WHERE precovenda < precotab")
    fun selectItensAutorizar():List<ItensPedido>

}