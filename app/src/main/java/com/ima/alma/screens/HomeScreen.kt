package com.ima.alma.screens

import android.app.Activity
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import coil.compose.AsyncImage
import com.ima.alma.R
import com.ima.alma.model.EntradaDiario
import com.ima.alma.ui.theme.*
import com.ima.alma.util.PdfManager
import com.ima.alma.util.PreferencesManager
import com.ima.alma.util.obtenerRutaThumbnail
import com.ima.alma.viewmodel.DiarioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DiarioViewModel,
    preferencesManager: PreferencesManager,
    fotoPortadaUri: String = "",
    temaPreferido: String = "sistema",
    onNavigateToEscribir: () -> Unit,
    onEntradaClick: (EntradaDiario) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val isDark = isSystemInDarkTheme() || temaPreferido == "ios_oscuro"

    val entradas by viewModel.todasLasEntradas.collectAsState(initial = emptyList())
    val idiomaActualState by preferencesManager.idiomaPreferido.collectAsState(initial = "es")

    var mostrarSettingsSheet by remember { mutableStateOf(false) }
    var exportandoPdf by remember { mutableStateOf(false) }

    // Estado persistente del modo de vista (Lista Compacta vs. Cuadrícula)
    var esCuadricula by rememberSaveable { mutableStateOf(false) }

    // Animación de Apertura de la App (App Launch Reveal)
    var iniciarAnimacion by remember { mutableStateOf(false) }
    val escalaEntrada by animateFloatAsState(
        targetValue = if (iniciarAnimacion) 1f else 0.94f,
        animationSpec = IosMotion.SheetSpring,
        label = "EscalaApertura"
    )
    val alfaEntrada by animateFloatAsState(
        targetValue = if (iniciarAnimacion) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "AlfaApertura"
    )

    LaunchedEffect(Unit) {
        iniciarAnimacion = true
    }

    // Rotación suave del icono del idioma
    val rotacionIconoIdioma by animateFloatAsState(
        targetValue = if (idiomaActualState == "en") 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.75f),
        label = "RotacionIdioma"
    )

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uriDestino ->
        uriDestino?.let { uri ->
            exportandoPdf = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    PdfManager.exportarDiarioAPdf(
                        context = context,
                        uriDestino = uri,
                        entradas = entradas,
                        fotoPortadaUri = fotoPortadaUri
                    )
                } finally {
                    exportandoPdf = false
                }
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    val entradasFiltradas = remember(entradas, searchQuery) {
        if (searchQuery.isBlank()) {
            entradas
        } else {
            val query = searchQuery.trim().lowercase()
            entradas.filter { entrada ->
                entrada.texto.lowercase().contains(query) ||
                (entrada.hashtags?.lowercase()?.contains(query) == true)
            }
        }
    }

    val entradasAgrupadas = remember(entradasFiltradas, idiomaActualState) {
        entradasFiltradas.groupBy { entrada ->
            obtenerTituloSeccionFecha(entrada.fechaMilisegundos)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Transición Óptica al Cambiar Idioma (Language Morph)
        AnimatedContent(
            targetState = idiomaActualState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 50)) +
                 scaleIn(initialScale = 0.98f, animationSpec = IosMotion.SheetSpring))
                    .togetherWith(
                        fadeOut(animationSpec = tween(150))
                    )
            },
            label = "TransicionIdioma",
            modifier = Modifier.fillMaxSize()
        ) { idiomaAct ->
            key(idiomaAct) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = escalaEntrada
                            scaleY = escalaEntrada
                            alpha = alfaEntrada
                        }
                        .padding(horizontal = 20.dp)
                ) {

                    Spacer(modifier = Modifier.height(60.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = TextStyle(
                                    fontFamily = SfProRounded,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 34.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.by_author),
                                style = Typography.labelSmall.copy(letterSpacing = 1.2.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { esCuadricula = !esCuadricula },
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = if (esCuadricula) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                    contentDescription = "Cambiar vista",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(
                                onClick = { mostrarSettingsSheet = true },
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = stringResource(R.string.settings_title),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .graphicsLayer { rotationZ = rotacionIconoIdioma }
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.search_placeholder),
                                            style = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tarjeta de Reflexión Editorial
                    TarjetaReflexion()

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!esCuadricula) {
                        // Modo Lista Compacta (Horizontal)
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            entradasAgrupadas.forEach { (tituloSeccion, listaEntradas) ->
                                item(key = "header_$tituloSeccion") {
                                    Text(
                                        text = tituloSeccion,
                                        style = TextStyle(
                                            fontFamily = SfProRounded,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 12.dp)
                                    )
                                }

                                items(listaEntradas, key = { it.id }) { entrada ->
                                    EntradaCardCompacta(
                                        entrada = entrada,
                                        isDark = isDark,
                                        onClick = { onEntradaClick(entrada) }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    } else {
                        // Modo Cuadrícula (Poster Grid)
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(entradasFiltradas, key = { it.id }) { entrada ->
                                EntradaCard(
                                    entrada = entrada,
                                    isDark = isDark,
                                    onClick = { onEntradaClick(entrada) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }

        BotonFlotanteAlma(
            onClick = onNavigateToEscribir,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )

        if (mostrarSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { mostrarSettingsSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = Typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.theme_appearance_title),
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val opcionesTema = listOf(
                        "sistema" to stringResource(R.string.theme_system),
                        "ios_claro" to stringResource(R.string.theme_light),
                        "ios_oscuro" to stringResource(R.string.theme_dark),
                        "material_you" to stringResource(R.string.theme_material_you)
                    )

                    opcionesTema.forEach { (clave, etiqueta) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        preferencesManager.guardarTemaPreferido(clave)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (temaPreferido == clave),
                                onClick = {
                                    coroutineScope.launch {
                                        preferencesManager.guardarTemaPreferido(clave)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = etiqueta,
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.language_title),
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = "Idioma",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer { rotationZ = rotacionIconoIdioma }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val opcionesIdioma = listOf(
                        "es" to "Español",
                        "en" to "English"
                    )

                    opcionesIdioma.forEach { (codigo, nombre) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        preferencesManager.guardarIdiomaPreferido(codigo)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val localeManager = context.getSystemService(LocaleManager::class.java)
                                            localeManager?.applicationLocales = LocaleList(Locale.forLanguageTag(codigo))
                                        } else {
                                            AppCompatDelegate.setApplicationLocales(
                                                LocaleListCompat.forLanguageTags(codigo)
                                            )
                                        }
                                        (context as? Activity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (idiomaActualState == codigo),
                                onClick = {
                                    coroutineScope.launch {
                                        preferencesManager.guardarIdiomaPreferido(codigo)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val localeManager = context.getSystemService(LocaleManager::class.java)
                                            localeManager?.applicationLocales = LocaleList(Locale.forLanguageTag(codigo))
                                        } else {
                                            AppCompatDelegate.setApplicationLocales(
                                                LocaleListCompat.forLanguageTags(codigo)
                                            )
                                        }
                                        (context as? Activity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = nombre,
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        onClick = {
                            if (!exportandoPdf) {
                                mostrarSettingsSheet = false
                                exportPdfLauncher.launch("Alma_Diario_Personal.pdf")
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (exportandoPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.PictureAsPdf,
                                    contentDescription = "Exportar a PDF",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.export_pdf_title),
                                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.export_pdf_subtitle),
                                    style = Typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ALMA v1.0",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.by_author) + " • Un diario personal e íntimo",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

fun obtenerTituloSeccionFecha(fechaMilis: Long): String {
    val ahora = Calendar.getInstance()
    val fecha = Calendar.getInstance().apply { timeInMillis = fechaMilis }

    val esMismoAno = (ahora.get(Calendar.YEAR) == fecha.get(Calendar.YEAR))
    val esMismoDia = esMismoAno && (ahora.get(Calendar.DAY_OF_YEAR) == fecha.get(Calendar.DAY_OF_YEAR))

    val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val esAyer = esMismoAno && (ayer.get(Calendar.DAY_OF_YEAR) == fecha.get(Calendar.DAY_OF_YEAR))

    return when {
        esMismoDia -> "HOY"
        esAyer -> "AYER"
        else -> {
            val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault())
            sdf.format(Date(fechaMilis)).uppercase()
        }
    }
}

// Tarjeta de Lista Compacta (Horizontal 92.dp)
@Composable
fun EntradaCardCompacta(
    entrada: EntradaDiario,
    isDark: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("EEEE, d MMMM • HH:mm", Locale.getDefault())
    val fechaHoraFormat = sdf.format(Date(entrada.fechaMilisegundos)).uppercase()

    val primeraFoto = entrada.obtenerTodasLasFotos().firstOrNull()

    val cardModifier = if (isDark) {
        Modifier
            .fillMaxWidth()
            .height(92.dp)
            .border(0.5.dp, Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    } else {
        Modifier
            .fillMaxWidth()
            .height(92.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
            .clickable { onClick() }
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Izquierda: Miniatura de la primera foto (76.dp con esquinas de 12.dp) o icono sutil de calendario
            if (!primeraFoto.isNullOrBlank()) {
                val thumbnailFile = remember(primeraFoto) { obtenerRutaThumbnail(context, primeraFoto) }
                val modelToLoad = if (thumbnailFile.exists()) thumbnailFile else primeraFoto

                AsyncImage(
                    model = modelToLoad,
                    contentDescription = "Miniatura entrada",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Derecha (weight 1f): Columna interna con la fecha en negrita y 2 líneas de texto recortadas con puntos suspensivos
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = fechaHoraFormat,
                    style = TextStyle(
                        fontFamily = SfProRounded,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = entrada.texto.ifBlank { "Sin texto..." },
                    style = TextStyle(
                        fontFamily = SfProRounded,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Tarjeta de Entrada Póster
@Composable
fun EntradaCard(
    entrada: EntradaDiario,
    isDark: Boolean = false,
    onClick: () -> Unit
) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val horaFormat = sdf.format(Date(entrada.fechaMilisegundos))

    val fuenteIos = SfProRounded

    val animoIcon = when (entrada.emojiAnimo) {
        "muy_feliz", "😃", "🥰" -> Icons.Rounded.SentimentVerySatisfied
        "feliz", "😊" -> Icons.Rounded.SentimentSatisfied
        "neutral", "😐" -> Icons.Rounded.SentimentNeutral
        "triste", "😔" -> Icons.Rounded.SentimentDissatisfied
        "enojado", "😡" -> Icons.Rounded.SentimentVeryDissatisfied
        "inspirado", "✨" -> Icons.Rounded.AutoAwesome
        else -> null
    }

    val cardModifier = if (isDark) {
        Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { onClick() }
    } else {
        Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { onClick() }
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (!entrada.fotoUri.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = entrada.fotoUri,
                    contentDescription = "Imagen de la nota",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (animoIcon != null) {
                            Icon(
                                imageVector = animoIcon,
                                contentDescription = "Ánimo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = horaFormat,
                            style = TextStyle(
                                fontFamily = SfProRounded,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = entrada.texto,
                        style = TextStyle(
                            fontFamily = fuenteIos,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!entrada.hashtags.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entrada.hashtags,
                            style = TextStyle(
                                fontFamily = fuenteIos,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (animoIcon != null) {
                        Icon(
                            imageVector = animoIcon,
                            contentDescription = "Ánimo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = horaFormat,
                        style = TextStyle(
                            fontFamily = SfProRounded,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = entrada.texto,
                    style = TextStyle(
                        fontFamily = fuenteIos,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (!entrada.hashtags.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = entrada.hashtags,
                        style = TextStyle(
                            fontFamily = fuenteIos,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}
