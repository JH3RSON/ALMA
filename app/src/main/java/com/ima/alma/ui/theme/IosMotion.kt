package com.ima.alma.ui.theme

import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset

object IosMotion {
    // El resorte estándar de Apple: rápido, orgánico y con frenado suave sin rebotes excesivos
    val SheetSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = 380f
    )
    val OffsetSpring = spring<IntOffset>(
        dampingRatio = 0.84f,
        stiffness = 400f
    )
    val ElementPopSpring = spring<Float>(
        dampingRatio = 0.70f,
        stiffness = 500f
    )
}
