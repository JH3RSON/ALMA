package com.ima.alma.util

import android.content.Context
import com.ima.alma.model.CitaFilosofica
import org.json.JSONArray

object GestorCitas {
    private var citasMemoria: List<CitaFilosofica> = emptyList()

    private val citaDefault = CitaFilosofica(
        id = 1,
        texto = "No pierdas más tiempo discutiendo lo que debería ser un buen hombre. Sé uno.",
        autor = "Marco Aurelio",
        corriente = "ESTOICISMO"
    )

    fun inicializar(context: Context) {
        if (citasMemoria.isEmpty()) {
            try {
                val jsonString = context.assets.open("citas.json").bufferedReader().use { it.readText() }
                citasMemoria = parsearCitas(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                citasMemoria = listOf(citaDefault)
            }
        }
    }

    private fun parsearCitas(jsonString: String): List<CitaFilosofica> {
        val lista = mutableListOf<CitaFilosofica>()
        return try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    CitaFilosofica(
                        id = obj.optInt("id", i + 1),
                        texto = obj.optString("texto", ""),
                        autor = obj.optString("autor", ""),
                        corriente = obj.optString("corriente", "PENSAMIENTO")
                    )
                )
            }
            if (lista.isNotEmpty()) lista else listOf(citaDefault)
        } catch (e: Exception) {
            e.printStackTrace()
            listOf(citaDefault)
        }
    }

    fun obtenerCitaAleatoria(context: Context? = null, excluirId: Int = -1): CitaFilosofica {
        if (citasMemoria.isEmpty() && context != null) {
            inicializar(context)
        }
        val disponibles = citasMemoria.filter { it.id != excluirId }
        return if (disponibles.isNotEmpty()) disponibles.random() else (citasMemoria.firstOrNull() ?: citaDefault)
    }
}
