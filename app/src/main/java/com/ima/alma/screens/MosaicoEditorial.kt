package com.ima.alma.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ima.alma.model.FotoMedia
import com.ima.alma.ui.theme.SfProRounded
import com.ima.alma.util.obtenerRutaThumbnail

@Composable
fun MosaicoEditorial(
    fotos: List<FotoMedia>,
    onFotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (fotos.isEmpty()) return

    Box(modifier = modifier.fillMaxWidth()) {
        when (fotos.size) {
            1 -> {
                // 1 foto: Proporción dinámica con ContentScale.Fit y fondo suave translúcido
                val context = LocalContext.current
                val foto = fotos[0]
                val thumbnailFile = remember(foto.uri) { obtenerRutaThumbnail(context, foto.uri) }
                val modelToLoad = if (thumbnailFile.exists()) thumbnailFile else foto.uri

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
                        contentDescription = "Foto destacada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onFotoClick(foto.uri) },
                        contentScale = ContentScale.Fit
                    )
                }
            }
            2 -> {
                // 2 fotos: Row dividido equitativamente (peso 1f cada una, altura 200.dp, espaciado 8.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FotoEditorialItem(
                        foto = fotos[0],
                        onFotoClick = onFotoClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                    )
                    FotoEditorialItem(
                        foto = fotos[1],
                        onFotoClick = onFotoClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            }
            3 -> {
                // 3 fotos: Row asimétrico
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FotoEditorialItem(
                        foto = fotos[0],
                        onFotoClick = onFotoClick,
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                    )

                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FotoEditorialItem(
                            foto = fotos[1],
                            onFotoClick = onFotoClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(106.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        FotoEditorialItem(
                            foto = fotos[2],
                            onFotoClick = onFotoClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(106.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }
                }
            }
            4 -> {
                // 4 fotos: Cuadrícula 2x2
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FotoEditorialItem(
                            foto = fotos[0],
                            onFotoClick = onFotoClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                        )
                        FotoEditorialItem(
                            foto = fotos[1],
                            onFotoClick = onFotoClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FotoEditorialItem(
                            foto = fotos[2],
                            onFotoClick = onFotoClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                        )
                        FotoEditorialItem(
                            foto = fotos[3],
                            onFotoClick = onFotoClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                }
            }
            else -> {
                // 5+ fotos
                val context = LocalContext.current
                val fotosExtra = fotos.size - 3
                val thumbnailFile = remember(fotos[2].uri) { obtenerRutaThumbnail(context, fotos[2].uri) }
                val modelToLoad = if (thumbnailFile.exists()) thumbnailFile else fotos[2].uri

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FotoEditorialItem(
                        foto = fotos[0],
                        onFotoClick = onFotoClick,
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                    )

                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FotoEditorialItem(
                            foto = fotos[1],
                            onFotoClick = onFotoClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(106.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(106.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onFotoClick(fotos[2].uri) }
                        ) {
                            AsyncImage(
                                model = modelToLoad,
                                contentDescription = "Foto adicional",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$fotosExtra",
                                    style = TextStyle(
                                        fontFamily = SfProRounded,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FotoEditorialItem(
    foto: FotoMedia,
    onFotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val thumbnailFile = remember(foto.uri) { obtenerRutaThumbnail(context, foto.uri) }
    val modelToLoad = if (thumbnailFile.exists()) thumbnailFile else foto.uri

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
            contentDescription = "Foto editorial",
            modifier = Modifier
                .fillMaxSize()
                .clickable { onFotoClick(foto.uri) },
            contentScale = ContentScale.Crop
        )
    }
}
