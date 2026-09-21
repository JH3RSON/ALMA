package com.ima.alma.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ima.alma.util.obtenerRutaThumbnail

@Composable
fun MosaicoFotos(
    fotos: List<String>,
    onEliminarFoto: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (fotos.isEmpty()) return

    Box(modifier = modifier.fillMaxWidth()) {
        when (fotos.size) {
            1 -> {
                // 1 Foto: Proporción dinámica con ContentScale.Fit y fondo suave translúcido
                val context = LocalContext.current
                val fotoUri = fotos[0]
                val thumbnailFile = remember(fotoUri) { obtenerRutaThumbnail(context, fotoUri) }
                val modelToLoad = if (thumbnailFile.exists()) thumbnailFile else fotoUri

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = modelToLoad,
                        contentDescription = "Foto única",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(22.dp)),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = { onEliminarFoto(0) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .background(Color(0x66000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Quitar foto",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            2 -> {
                // 2 Fotos: Row equitativo 50/50
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FotoItemConBotonBorrar(
                        uri = fotos[0],
                        onBorrar = { onEliminarFoto(0) },
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                    FotoItemConBotonBorrar(
                        uri = fotos[1],
                        onBorrar = { onEliminarFoto(1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            }
            else -> {
                // 3 Fotos: Principal 60% izquierda, 2 apiladas 40% derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FotoItemConBotonBorrar(
                        uri = fotos[0],
                        onBorrar = { onEliminarFoto(0) },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(220.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )

                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FotoItemConBotonBorrar(
                            uri = fotos[1],
                            onBorrar = { onEliminarFoto(1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        FotoItemConBotonBorrar(
                            uri = fotos[2],
                            onBorrar = { onEliminarFoto(2) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FotoItemConBotonBorrar(
    uri: String,
    onBorrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val thumbnailFile = remember(uri) { obtenerRutaThumbnail(context, uri) }
    val modelToLoad = if (thumbnailFile.exists()) thumbnailFile else uri

    Box(
        modifier = modifier
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                RoundedCornerShape(18.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AsyncImage(
            model = modelToLoad,
            contentDescription = "Foto del mosaico",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onBorrar,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(28.dp)
                .background(Color(0x66000000), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Quitar foto",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
