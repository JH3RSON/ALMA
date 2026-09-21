package com.ima.alma.util

import android.content.Context

object FrasesProvider {
    fun obtenerFraseAleatoria(context: Context? = null): String {
        return BancoReflexiones.obtenerAleatoria()
    }
}
