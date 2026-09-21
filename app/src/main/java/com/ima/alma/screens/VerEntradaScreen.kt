package com.ima.alma.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ima.alma.model.EntradaDiario
import com.ima.alma.model.FotoMedia
import com.ima.alma.ui.theme.SfProRounded
import com.ima.alma.ui.theme.Typography
import com.ima.alma.viewmodel.DiarioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerEntradaScreen(
    entrada: EntradaDiario,
    viewModel: DiarioViewModel,
    onNavigateBack: () -> Unit,
    onEditarClick: (EntradaDiario) -> Unit
) {
    BackHandler { onNavigateBack() }

    var mostrarDialogoBorrar by remember { mutableStateOf(false) }
    var fotoParaVisor by remember { mutableStateOf<String?>(null) }

    val sdfFecha = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault())
    val fechaFormateada = sdfFecha.format(Date(entrada.fechaMilisegundos)).lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
    val horaFormat = sdfHora.format(Date(entrada.fechaMilisegundos))

    val todasLasFotosMedia = remember(entrada) {
        entrada.obtenerTodasLasFotos().map { uri -> FotoMedia(uri = uri) }
    }

    val animoIcon = when (entrada.emojiAnimo) {
        "muy_feliz", "😃", "🥰" -> Icons.Rounded.SentimentVerySatisfied
        "feliz", "😊" -> Icons.Rounded.SentimentSatisfied
        "neutral", "😐" -> Icons.Rounded.SentimentNeutral
        "triste", "😔" -> Icons.Rounded.SentimentDissatisfied
        "enojado", "😡" -> Icons.Rounded.SentimentVeryDissatisfied
        "inspirado", "✨" -> Icons.Rounded.AutoAwesome
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditarClick(entrada) }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar nota",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { mostrarDialogoBorrar = true }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Eliminar nota",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Cabecera con Fecha Editorial Gigante
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onEditarClick(entrada) }
                ) {
                    Text(
                        text = fechaFormateada,
                        style = TextStyle(
                            fontFamily = SfProRounded,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.8).sp,
                            lineHeight = 36.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 6.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        if (animoIcon != null) {
                            Icon(
                                imageVector = animoIcon,
                                contentDescription = "Ánimo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = horaFormat,
                            style = TextStyle(
                                fontFamily = SfProRounded,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Bloque principal de texto
                if (entrada.texto.isNotBlank()) {
                    Text(
                        text = entrada.texto,
                        style = TextStyle(
                            fontFamily = SfProRounded,
                            fontSize = 18.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditarClick(entrada) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Inserción del MosaicoEditorial Adaptativo
                if (todasLasFotosMedia.isNotEmpty()) {
                    MosaicoEditorial(
                        fotos = todasLasFotosMedia,
                        onFotoClick = { uri -> fotoParaVisor = uri },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (!entrada.hashtags.isNullOrBlank()) {
                    Text(
                        text = entrada.hashtags,
                        style = TextStyle(
                            fontFamily = SfProRounded,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.clickable { onEditarClick(entrada) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            if (fotoParaVisor != null) {
                VisorImagenDialog(
                    uri = fotoParaVisor!!,
                    onDismiss = { fotoParaVisor = null }
                )
            }

            if (mostrarDialogoBorrar) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoBorrar = false },
                    title = {
                        Text(
                            text = "¿Eliminar entrada?",
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "Esta acción no se puede deshacer.",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarDialogoBorrar = false
                                viewModel.eliminar(entrada)
                                onNavigateBack()
                            }
                        ) {
                            Text(
                                text = "Eliminar",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoBorrar = false }) {
                            Text(text = "Cancelar", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 0.dp
                )
            }
        }
    }
}
