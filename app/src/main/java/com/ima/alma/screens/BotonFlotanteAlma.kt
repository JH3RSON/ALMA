package com.ima.alma.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BotonFlotanteAlma(
    onClick: () -> Unit,
    estaEscribiendo: Boolean = false,
    modifier: Modifier = Modifier
) {
    BotonRelieveIos(
        onClick = onClick,
        modifier = modifier,
        estaEscribiendo = estaEscribiendo
    )
}
