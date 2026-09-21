package com.ima.alma.model

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        for (item in value) {
            jsonArray.put(item)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        return try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromBloqueList(bloques: List<BloqueEntrada>?): String {
        if (bloques.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        for (bloque in bloques) {
            val jsonObject = JSONObject()
            when (bloque) {
                is BloqueEntrada.Texto -> {
                    jsonObject.put("tipo", "texto")
                    jsonObject.put("contenido", bloque.contenido)
                }
                is BloqueEntrada.FotoLateral -> {
                    jsonObject.put("tipo", "foto_lateral")
                    jsonObject.put("uri", bloque.uri)
                    jsonObject.put("textoSecundario", bloque.textoSecundario)
                    jsonObject.put("alineacionIzquierda", bloque.alineacionIzquierda)
                }
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toBloqueList(value: String?): List<BloqueEntrada> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<BloqueEntrada>()
        return try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                when (jsonObject.optString("tipo")) {
                    "texto" -> {
                        list.add(
                            BloqueEntrada.Texto(
                                contenido = jsonObject.optString("contenido", "")
                            )
                        )
                    }
                    "foto_lateral" -> {
                        list.add(
                            BloqueEntrada.FotoLateral(
                                uri = jsonObject.optString("uri", ""),
                                textoSecundario = jsonObject.optString("textoSecundario", ""),
                                alineacionIzquierda = jsonObject.optBoolean("alineacionIzquierda", true)
                            )
                        )
                    }
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
