package com.ima.alma.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ima.alma.ui.theme.IosMotion

@Composable
fun BotonRelieveIos(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    estaEscribiendo: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    var estaPresionado by remember { mutableStateOf(false) }

    val escala by animateFloatAsState(
        targetValue = if (estaPresionado) 0.92f else 1f,
        animationSpec = IosMotion.ElementPopSpring,
        label = "BotonRelieveEscala"
    )

    val rotacion by animateFloatAsState(
        targetValue = if (estaEscribiendo) 45f else 0f,
        animationSpec = IosMotion.ElementPopSpring,
        label = "PlusRotation"
    )

    // Degradado convexo de relieve táctil que se invierte sutilmente al presionar
    val coloresGradiente = if (estaPresionado) {
        listOf(Color(0xFF4745B8), Color(0xFF6E6CE8))
    } else {
        listOf(Color(0xFF6E6CE8), Color(0xFF4745B8))
    }

    Box(
        modifier = modifier
            .size(58.dp)
            .shadow(
                elevation = 14.dp,
                shape = CircleShape,
                spotColor = Color(0xFF4745B8).copy(alpha = 0.45f),
                ambientColor = Color(0x20000000)
            )
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .clip(CircleShape)
            .background(Brush.verticalGradient(coloresGradiente))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.55f), Color.Transparent)
                ),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        estaPresionado = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val exitoso = tryAwaitRelease()
                        estaPresionado = false
                        if (exitoso) { onClick() }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Nueva entrada",
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { rotationZ = rotacion }
        )
    }
}
