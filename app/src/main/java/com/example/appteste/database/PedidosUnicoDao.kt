package com.example.appteste.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PedidosUnicoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(pedidoUnico: PedidoUnico)

    @Query("SELECT * FROM PEDIDOUNICO LIMIT 1")
    fun findPedido():PedidoUnico


    @Query("DELETE FROM PEDIDOUNICO ")
    fun delete()


}