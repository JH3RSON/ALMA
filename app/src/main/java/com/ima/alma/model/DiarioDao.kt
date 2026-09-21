package com.ima.alma.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiarioDao {
    @Insert
    suspend fun insertar(entrada: EntradaDiario)

    @Update
    suspend fun actualizar(entrada: EntradaDiario)

    @Delete
    suspend fun eliminar(entrada: EntradaDiario)

    @Query("SELECT * FROM entradas_diario ORDER BY fechaMilisegundos DESC")
    fun obtenerTodas(): Flow<List<EntradaDiario>>

    @Query("SELECT * FROM entradas_diario WHERE texto LIKE '%' || :query || '%' OR hashtags LIKE '%' || :query || '%' ORDER BY fechaMilisegundos DESC")
    fun buscarEntradas(query: String): Flow<List<EntradaDiario>>
}
