package com.ima.alma.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ima.alma.model.CitaFilosofica
import com.ima.alma.ui.theme.IosMotion
import com.ima.alma.ui.theme.SfProRounded
import com.ima.alma.ui.theme.iosPressEffect
import com.ima.alma.util.GestorCitasMazo
import kotlinx.coroutines.launch

@Composable
fun TarjetaReflexion(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val esOscuro = isSystemInDarkTheme()

    var citaActual by remember {
        mutableStateOf(
            CitaFilosofica(
                id = 1,
                texto = "No pierdas más tiempo discutiendo lo que debería ser un buen hombre. Sé uno.",
                autor = "Marco Aurelio",
                corriente = "Estoicismo"
            )
        )
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            citaActual = GestorCitasMazo.obtenerSiguienteCita(context)
        }
    }

    var rotacionGrados by remember { mutableFloatStateOf(0f) }

    val rotacionAnimada by animateFloatAsState(
        targetValue = rotacionGrados,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "ReflexionRotacion"
    )

    // Colores del System Gray 5 Oficial de Apple (#E5E5EA en claro, #2C2C2E en oscuro)
    val colorFondo = if (esOscuro) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val colorTextoPrincipal = if (esOscuro) Color(0xFFFFFFFF) else Color(0xFF000000)
    val colorTextoSecundario = if (esOscuro) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val colorTextoPildora = if (esOscuro) Color(0xFFEBEBF5).copy(alpha = 0.8f) else Color(0xFF3C3C43).copy(alpha = 0.9f)
    val colorFondoPildora = if (esOscuro) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.65f)
    val colorBorde = if (esOscuro) Color(0x26FFFFFF) else Color(0x0F000000)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colorFondo)
            .border(
                BorderStroke(0.5.dp, colorBorde),
                RoundedCornerShape(22.dp)
            )
            .iosPressEffect(escalaPresionado = 0.97f) {
                rotacionGrados += 360f
                coroutineScope.launch {
                    citaActual = GestorCitasMazo.obtenerSiguienteCita(context)
                }
            }
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Encabezado sutil: La corriente en mayúsculas
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colorFondoPildora)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = citaActual.corriente.uppercase(),
                        style = TextStyle(
                            fontFamily = SfProRounded,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            color = colorTextoPildora
                        )
                    )
                }

                // Icono sutil de refresco
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Siguiente cita filosófica",
                    tint = colorTextoPrincipal.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = rotacionAnimada }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animación de cambio de cita con desvanecimiento y pequeña escala
            AnimatedContent(
                targetState = citaActual,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.97f, animationSpec = IosMotion.ElementPopSpring)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "CitaMorph"
            ) { cita ->
                Column {
                    Text(
                        text = "«${cita.texto}»",
                        style = TextStyle(
                            fontFamily = SfProRounded,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp,
                            color = colorTextoPrincipal
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "— ${cita.autor}",
                        style = TextStyle(
                            fontFamily = SfProRounded,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorTextoSecundario
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
