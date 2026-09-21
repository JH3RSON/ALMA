package com.ima.alma.model

sealed class BloqueEntrada {
    data class Texto(var contenido: String) : BloqueEntrada()
    data class FotoLateral(
        val uri: String,
        var textoSecundario: String = "",
        val alineacionIzquierda: Boolean = true // true: foto a la izq, false: foto a la der
    ) : BloqueEntrada()
}
