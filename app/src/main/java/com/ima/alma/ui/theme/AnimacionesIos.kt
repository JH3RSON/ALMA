package com.ima.alma.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

enum class EstadoBoton { Presionado, Reposo }

@Composable
fun Modifier.iosPressEffect(
    escalaPresionado: Float = 0.94f,
    onTap: () -> Unit
): Modifier {
    var estadoActual by remember { mutableStateOf(EstadoBoton.Reposo) }
    val escala by animateFloatAsState(
        targetValue = if (estadoActual == EstadoBoton.Presionado) escalaPresionado else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BotonBounce"
    )

    return this
        .graphicsLayer {
            scaleX = escala
            scaleY = escala
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    estadoActual = EstadoBoton.Presionado
                    val exitoso = tryAwaitRelease()
                    estadoActual = EstadoBoton.Reposo
                    if (exitoso) { onTap() }
                }
            )
        }
}
