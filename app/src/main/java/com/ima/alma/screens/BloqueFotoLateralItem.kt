package com.ima.alma.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ima.alma.model.BloqueEntrada
import com.ima.alma.ui.theme.SfProRounded

@Composable
fun BloqueFotoLateralItem(
    bloque: BloqueEntrada.FotoLateral,
    onTextoChange: (String) -> Unit,
    onEliminarFoto: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        val composableFoto = @Composable {
            Box(modifier = Modifier.weight(0.42f)) {
                AsyncImage(
                    model = bloque.uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
                // Botón circular 'X' en la esquina superior para borrar la foto
                IconButton(
                    onClick = onEliminarFoto,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .background(Color(0x66000000), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        val composableTexto = @Composable {
            BasicTextField(
                value = bloque.textoSecundario,
                onValueChange = onTextoChange,
                modifier = Modifier.weight(0.58f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = SfProRounded,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (bloque.textoSecundario.isEmpty()) {
                        Text("Escribe junto a la foto...", color = Color.Gray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
        }

        if (bloque.alineacionIzquierda) {
            composableFoto()
            composableTexto()
        } else {
            composableTexto()
            composableFoto()
        }
    }
}
