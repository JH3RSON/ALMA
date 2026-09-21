package com.ima.alma.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entradas_diario")
data class EntradaDiario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String? = null,
    val texto: String = "",
    val emojiAnimo: String = "",
    val fechaMilisegundos: Long = System.currentTimeMillis(),
    val fotoUri: String? = null,
    val fotosUris: List<String> = emptyList(),
    val hashtags: String? = null,
    val bloques: List<BloqueEntrada> = emptyList()
) {
    fun obtenerTodasLasFotos(): List<String> {
        val fotosDeBloques = bloques.filterIsInstance<BloqueEntrada.FotoLateral>().map { it.uri }
        val lista = (fotosUris + fotosDeBloques).toMutableList()
        if (!fotoUri.isNullOrBlank() && !lista.contains(fotoUri)) {
            lista.add(0, fotoUri)
        }
        return lista.distinct()
    }

    fun obtenerListaBloques(): List<BloqueEntrada> {
        if (bloques.isNotEmpty()) return bloques

        val listaBloquesFallback = mutableListOf<BloqueEntrada>()
        if (texto.isNotBlank()) {
            listaBloquesFallback.add(BloqueEntrada.Texto(texto))
        }
        val todasLasFotos = obtenerTodasLasFotos()
        todasLasFotos.forEachIndexed { index, uri ->
            listaBloquesFallback.add(
                BloqueEntrada.FotoLateral(
                    uri = uri,
                    textoSecundario = "",
                    alineacionIzquierda = index % 2 == 0
                )
            )
        }
        if (listaBloquesFallback.isEmpty()) {
            listaBloquesFallback.add(BloqueEntrada.Texto(""))
        }
        return listaBloquesFallback
    }
}
