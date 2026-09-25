package com.ima.alma.util

import android.content.Context
import com.ima.alma.R

object FrasesProvider {
    fun obtenerFraseAleatoria(context: Context? = null): String {
        if (context != null) {
            try {
                val frases = context.resources.getStringArray(R.array.frases_diario)
                if (frases.isNotEmpty()) {
                    return frases.random()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return BancoReflexiones.obtenerAleatoria()
    }
}
