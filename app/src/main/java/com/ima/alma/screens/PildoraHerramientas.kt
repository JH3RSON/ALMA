package com.ima.alma.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ima.alma.ui.theme.iosPressEffect

@Composable
fun PildoraHerramientas(
    animoSeleccionado: String?,
    fotosListaSize: Int,
    mostrarHashtags: Boolean,
    hashtagsTexto: String,
    onAnimoClick: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onHashtagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esOscuro = isSystemInDarkTheme()
    val limiteAlcanzado = fotosListaSize >= 3

    val colorIconoBase = if (esOscuro) Color(0xFFEBEBF5) else Color(0xFF3C3C43)
    val colorIconoActivo = MaterialTheme.colorScheme.primary
    val colorIconoDesactivado = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Surface(
        modifier = modifier
            .height(50.dp)
            .shadow(
                elevation = 18.dp,
                shape = CircleShape,
                spotColor = Color(0x33000000)
            )
            .border(
                BorderStroke(
                    0.6.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.10f))
                    )
                ),
                CircleShape
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Ánimo (Área táctil ergonómica de 44x44 dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .iosPressEffect(escalaPresionado = 0.85f, onTap = onAnimoClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddReaction,
                    contentDescription = "Ánimo",
                    tint = if (!animoSeleccionado.isNullOrBlank()) colorIconoActivo else colorIconoBase,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Cámara (Área táctil ergonómica de 44x44 dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .iosPressEffect(
                        escalaPresionado = 0.85f,
                        onTap = {
                            if (!limiteAlcanzado) onCameraClick()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Cámara",
                    tint = if (limiteAlcanzado) {
                        colorIconoDesactivado
                    } else if (fotosListaSize > 0) {
                        colorIconoActivo
                    } else {
                        colorIconoBase
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            // Galería Múltiple (Área táctil ergonómica de 44x44 dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .iosPressEffect(
                        escalaPresionado = 0.85f,
                        onTap = {
                            if (!limiteAlcanzado) onGalleryClick()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = "Galería Múltiple",
                    tint = if (limiteAlcanzado) {
                        colorIconoDesactivado
                    } else if (fotosListaSize > 0) {
                        colorIconoActivo
                    } else {
                        colorIconoBase
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            // Hashtag (Área táctil ergonómica de 44x44 dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .iosPressEffect(escalaPresionado = 0.85f, onTap = onHashtagClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tag,
                    contentDescription = "Hashtag",
                    tint = if (mostrarHashtags || hashtagsTexto.isNotBlank()) colorIconoActivo else colorIconoBase,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
