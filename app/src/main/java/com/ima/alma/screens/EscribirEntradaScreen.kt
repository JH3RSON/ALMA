package com.ima.alma.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ima.alma.R
import com.ima.alma.model.EntradaDiario
import com.ima.alma.ui.theme.SfProRounded
import com.ima.alma.util.FrasesProvider
import com.ima.alma.util.procesarYGuardarFotoEnStorage
import com.ima.alma.util.rememberMediaManager
import com.ima.alma.viewmodel.DiarioViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnimoOpcion(
    val clave: String,
    val icon: ImageVector,
    val descripcion: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscribirEntradaScreen(
    viewModel: DiarioViewModel,
    onNavigateBack: () -> Unit,
    entradaParaEditar: EntradaDiario? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val fuenteIosNormal = SfProRounded

    val configuracion = LocalConfiguration.current
    val esHorizontal = configuracion.orientation == Configuration.ORIENTATION_LANDSCAPE

    var textoState by remember(entradaParaEditar) {
        mutableStateOf(TextFieldValue(entradaParaEditar?.texto ?: ""))
    }

    var cuerpo by remember(entradaParaEditar) { mutableStateOf(entradaParaEditar?.texto ?: "") }

    // Estado con límite estricto de máximo 3 fotos
    var fotosLista by remember(entradaParaEditar) {
        mutableStateOf<List<String>>(
            (entradaParaEditar?.obtenerTodasLasFotos() ?: emptyList()).take(3)
        )
    }

    var hashtagsTexto by remember(entradaParaEditar) { mutableStateOf(entradaParaEditar?.hashtags ?: "") }

    var animoSeleccionado by remember(entradaParaEditar) { mutableStateOf<String?>(entradaParaEditar?.emojiAnimo) }
    var mostrarSelectorAnimo by remember { mutableStateOf(false) }

    var mostrarHashtags by remember(entradaParaEditar) {
        mutableStateOf(!entradaParaEditar?.hashtags.isNullOrBlank())
    }

    val focusRequesterHashtag = remember { FocusRequester() }

    LaunchedEffect(mostrarHashtags) {
        if (mostrarHashtags) {
            focusRequesterHashtag.requestFocus()
        }
    }

    // Aparición Escalonada del Contenido (Staggered Animation)
    var visibleHeader by remember { mutableStateOf(false) }
    var visibleFotos by remember { mutableStateOf(false) }
    var visibleTexto by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(30)
        visibleHeader = true
        delay(30)
        visibleFotos = true
        delay(30)
        visibleTexto = true
    }

    val headerOffsetY by animateDpAsState(
        targetValue = if (visibleHeader) 0.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "HeaderOffset"
    )
    val headerAlpha by animateFloatAsState(
        targetValue = if (visibleHeader) 1f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "HeaderAlpha"
    )

    val fotosOffsetY by animateDpAsState(
        targetValue = if (visibleFotos) 0.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "FotosOffset"
    )
    val fotosAlpha by animateFloatAsState(
        targetValue = if (visibleFotos) 1f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "FotosAlpha"
    )

    val textoOffsetY by animateDpAsState(
        targetValue = if (visibleTexto) 0.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "TextoOffset"
    )
    val textoAlpha by animateFloatAsState(
        targetValue = if (visibleTexto) 1f else 0f,
        animationSpec = spring(stiffness = 400f),
        label = "TextoAlpha"
    )

    var mostrarDialogoBorrar by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val maxScrollValue by remember { derivedStateOf { scrollState.maxValue } }
    LaunchedEffect(maxScrollValue) {
        if (maxScrollValue > 0) {
            scrollState.animateScrollTo(maxScrollValue)
        }
    }

    // Selector de fotos nativo con límite estricto de 3 imágenes
    val selectorFotos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val espacioRestante = 3 - fotosLista.size
            if (espacioRestante > 0) {
                val fotosAceptadas = uris.take(espacioRestante)
                fotosAceptadas.forEach { uri ->
                    val permanentUri = procesarYGuardarFotoEnStorage(context, uri)
                    val uriFinal = (permanentUri ?: uri).toString()
                    if (!fotosLista.contains(uriFinal)) {
                        fotosLista = (fotosLista + uriFinal).take(3)
                    }
                }
            }
        }
    }

    val ejecutarGuardadoInquebrantable = {
        if (cuerpo.isNotBlank() || fotosLista.isNotEmpty()) {
            val hashtagsFinales = hashtagsTexto.trim().takeIf { it.isNotBlank() }
            val animoFinal = animoSeleccionado ?: ""
            val primeraFoto = fotosLista.firstOrNull()

            if (entradaParaEditar != null) {
                val entradaActualizada = entradaParaEditar.copy(
                    texto = cuerpo,
                    emojiAnimo = animoFinal,
                    fotoUri = primeraFoto,
                    fotosUris = fotosLista,
                    hashtags = hashtagsFinales
                )
                viewModel.actualizar(entradaActualizada)
            } else {
                val nuevaEntrada = EntradaDiario(
                    texto = cuerpo,
                    emojiAnimo = animoFinal,
                    fechaMilisegundos = System.currentTimeMillis(),
                    fotoUri = primeraFoto,
                    fotosUris = fotosLista,
                    hashtags = hashtagsFinales
                )
                viewModel.insertar(nuevaEntrada)
            }
        }
    }

    val cerrarTecladoYVolver = {
        ejecutarGuardadoInquebrantable()
        focusManager.clearFocus()
        keyboardController?.hide()
        onNavigateBack()
    }

    BackHandler {
        cerrarTecladoYVolver()
    }

    DisposableEffect(lifecycleOwner, cuerpo, fotosLista, hashtagsTexto, animoSeleccionado) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                ejecutarGuardadoInquebrantable()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val opcionesAnimo = listOf(
        AnimoOpcion("muy_feliz", Icons.Rounded.SentimentVerySatisfied, "Muy Feliz"),
        AnimoOpcion("feliz", Icons.Rounded.SentimentSatisfied, "Feliz"),
        AnimoOpcion("neutral", Icons.Rounded.SentimentNeutral, "Neutral"),
        AnimoOpcion("triste", Icons.Rounded.SentimentDissatisfied, "Triste"),
        AnimoOpcion("enojado", Icons.Rounded.SentimentVeryDissatisfied, "Enojado"),
        AnimoOpcion("inspirado", Icons.Rounded.AutoAwesome, "Inspirado")
    )

    val mediaManager = rememberMediaManager(
        onPhotoCaptured = { uri ->
            uri?.let {
                if (fotosLista.size < 3) {
                    fotosLista = (fotosLista + it.toString()).distinct().take(3)
                }
            }
        },
        onPhotoSelected = { uri ->
            uri?.let {
                if (fotosLista.size < 3) {
                    fotosLista = (fotosLista + it.toString()).distinct().take(3)
                }
            }
        },
        onMultiplePhotosSelected = { uris ->
            val espacioRestante = 3 - fotosLista.size
            if (espacioRestante > 0) {
                val nuevas = uris.take(espacioRestante).map { it.toString() }
                fotosLista = (fotosLista + nuevas).distinct().take(3)
            }
        }
    )

    val fechaHeaderElegante = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault()).format(Date()).uppercase()
    }

    val fraseDelDia = remember { FrasesProvider.obtenerFraseAleatoria(context) }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { cerrarTecladoYVolver() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (entradaParaEditar != null) {
                        IconButton(onClick = {
                            mostrarDialogoBorrar = true
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Eliminar nota",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TextButton(onClick = {
                        cerrarTecladoYVolver()
                    }) {
                        Text(
                            text = stringResource(R.string.btn_done),
                            fontFamily = SfProRounded,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            if (esHorizontal) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight()
                            .padding(end = 12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = fechaHeaderElegante,
                            style = TextStyle(
                                fontFamily = SfProRounded,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "«$fraseDelDia»",
                            style = TextStyle(
                                fontFamily = fuenteIosNormal,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        if (fotosLista.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            MosaicoFotos(
                                fotos = fotosLista,
                                onEliminarFoto = { indice ->
                                    fotosLista = fotosLista.filterIndexed { index, _ -> index != indice }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight()
                            .verticalScroll(scrollState)
                            .padding(start = 12.dp)
                    ) {
                        BasicTextField(
                            value = textoState,
                            onValueChange = {
                                textoState = it
                                cuerpo = it.text
                            },
                            textStyle = TextStyle(
                                fontFamily = fuenteIosNormal,
                                fontSize = 17.sp,
                                lineHeight = 24.sp,
                                letterSpacing = (-0.4).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text,
                                autoCorrectEnabled = true
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 150.dp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (textoState.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.write_body_placeholder),
                                        style = TextStyle(
                                            fontFamily = fuenteIosNormal,
                                            fontSize = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                ) {
                    // 1. Cabecera de fecha y hora con animación escalonada
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationY = headerOffsetY.toPx()
                                alpha = headerAlpha
                            }
                    ) {
                        Text(
                            text = fechaHeaderElegante,
                            style = TextStyle(
                                fontFamily = SfProRounded,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        )

                        Text(
                            text = "«$fraseDelDia»",
                            style = TextStyle(
                                fontFamily = fuenteIosNormal,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        )
                    }

                    // 2. Mosaico de fotos superior con animación escalonada
                    if (fotosLista.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    translationY = fotosOffsetY.toPx()
                                    alpha = fotosAlpha
                                }
                        ) {
                            MosaicoFotos(
                                fotos = fotosLista,
                                onEliminarFoto = { indice ->
                                    fotosLista = fotosLista.filterIndexed { index, _ -> index != indice }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    AnimatedVisibility(
                        visible = mostrarSelectorAnimo,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            opcionesAnimo.forEach { opcion ->
                                val esSeleccionado = (animoSeleccionado == opcion.clave)
                                Surface(
                                    onClick = { animoSeleccionado = opcion.clave },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (esSeleccionado) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Icon(
                                        imageVector = opcion.icon,
                                        contentDescription = opcion.descripcion,
                                        tint = if (esSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp).size(26.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Campo de texto con animación escalonada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationY = textoOffsetY.toPx()
                                alpha = textoAlpha
                            }
                    ) {
                        BasicTextField(
                            value = textoState,
                            onValueChange = {
                                textoState = it
                                cuerpo = it.text
                            },
                            textStyle = TextStyle(
                                fontFamily = fuenteIosNormal,
                                fontSize = 17.sp,
                                lineHeight = 24.sp,
                                letterSpacing = (-0.4).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text,
                                autoCorrectEnabled = true
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 200.dp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (textoState.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.write_body_placeholder),
                                        style = TextStyle(
                                            fontFamily = fuenteIosNormal,
                                            fontSize = 17.sp,
                                            lineHeight = 24.sp,
                                            letterSpacing = (-0.4).sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    AnimatedVisibility(
                        visible = mostrarHashtags,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tag,
                                    contentDescription = "Hashtags",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = hashtagsTexto,
                                    onValueChange = { hashtagsTexto = it },
                                    textStyle = TextStyle(
                                        fontFamily = fuenteIosNormal,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done,
                                        capitalization = KeyboardCapitalization.None,
                                        keyboardType = KeyboardType.Text,
                                        autoCorrectEnabled = false
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequesterHashtag),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { innerTextField ->
                                        if (hashtagsTexto.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.write_hashtags_placeholder),
                                                style = TextStyle(
                                                    fontFamily = fuenteIosNormal,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }

                    // 5. Colchón inferior para que la última línea escrita flote siempre por encima de la píldora y del teclado
                    Spacer(modifier = Modifier.height(140.dp))
                }
            }

            // Capa 2 - Píldora Flotante Estilo Vidrio iOS
            PildoraHerramientas(
                animoSeleccionado = animoSeleccionado,
                fotosListaSize = fotosLista.size,
                mostrarHashtags = mostrarHashtags,
                hashtagsTexto = hashtagsTexto,
                onAnimoClick = { mostrarSelectorAnimo = !mostrarSelectorAnimo },
                onCameraClick = { mediaManager.openCamera() },
                onGalleryClick = {
                    selectorFotos.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onHashtagClick = { mostrarHashtags = !mostrarHashtags },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )

            if (mostrarDialogoBorrar) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoBorrar = false },
                    title = {
                        Text(
                            text = "¿Eliminar entrada?",
                            style = TextStyle(
                                fontFamily = SfProRounded,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "Esta acción no se puede deshacer.",
                            style = TextStyle(
                                fontFamily = fuenteIosNormal,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarDialogoBorrar = false
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                if (entradaParaEditar != null) {
                                    viewModel.eliminar(entradaParaEditar)
                                }
                                onNavigateBack()
                            }
                        ) {
                            Text(
                                text = "Eliminar",
                                style = TextStyle(
                                    fontFamily = SfProRounded,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoBorrar = false }) {
                            Text(
                                text = "Cancelar",
                                style = TextStyle(
                                    fontFamily = fuenteIosNormal,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
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
