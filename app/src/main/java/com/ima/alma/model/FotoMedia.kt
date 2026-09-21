package com.ima.alma.model

data class FotoMedia(
    val uri: String,
    val ancho: Int = 1,
    val alto: Int = 1
) {
    val aspectRatio: Float get() = if (ancho > 0 && alto > 0) ancho.toFloat() / alto.toFloat() else 1f
    val esVertical: Boolean get() = alto > ancho
}
