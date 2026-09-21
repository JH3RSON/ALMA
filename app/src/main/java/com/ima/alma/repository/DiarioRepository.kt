package com.ima.alma.repository

import com.ima.alma.model.DiarioDao
import com.ima.alma.model.EntradaDiario
import kotlinx.coroutines.flow.Flow

class DiarioRepository(private val diarioDao: DiarioDao) {

    val todasLasEntradas: Flow<List<EntradaDiario>> = diarioDao.obtenerTodas()

    suspend fun insertar(entrada: EntradaDiario) {
        diarioDao.insertar(entrada)
    }

    suspend fun actualizar(entrada: EntradaDiario) {
        diarioDao.actualizar(entrada)
    }

    suspend fun eliminar(entrada: EntradaDiario) {
        diarioDao.eliminar(entrada)
    }
}