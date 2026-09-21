package com.ima.alma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.ima.alma.model.EntradaDiario
import com.ima.alma.screens.BienvenidaScreen
import com.ima.alma.screens.EscribirEntradaScreen
import com.ima.alma.screens.HomeScreen
import com.ima.alma.screens.VerEntradaScreen
import com.ima.alma.ui.theme.AlmaTheme
import com.ima.alma.ui.theme.FondoSistemaLight
import com.ima.alma.ui.theme.IosMotion
import com.ima.alma.ui.theme.iosFontFamily
import com.ima.alma.util.AlarmaManagerHelper
import com.ima.alma.util.PreferencesManager
import com.ima.alma.viewmodel.DiarioViewModel
import com.ima.alma.viewmodel.DiarioViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forzar Insets de Ventana (Fix del teclado definitivo)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        // Programar la notificación exacta diaria a las 5:00 PM
        AlarmaManagerHelper.programarNotificacionDiaria(this)

        val app = application as AlmaApplication
        val factory = DiarioViewModelFactory(app.repository)
        val viewModel = ViewModelProvider(this, factory)[DiarioViewModel::class.java]
        val preferencesManager = PreferencesManager(this)

        setContent {
            // Lectura asíncrona de DataStore (clave: es_primer_inicio)
            val esPrimerInicioState by preferencesManager.esPrimerInicio.collectAsState(initial = null)
            val fotoPortadaUriState by preferencesManager.fotoPortadaUri.collectAsState(initial = "")
            val temaActual by preferencesManager.temaPreferido.collectAsState(initial = "sistema")

            val coroutineScope = rememberCoroutineScope()

            AlmaTheme(temaPreferido = temaActual ?: "sistema") {
                // Inyección Definitiva de Fuente global usando LocalTextStyle
                CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = iosFontFamily)) {
                    var pantallaActual by remember { mutableStateOf("home") }
                    var notaSeleccionada by remember { mutableStateOf<EntradaDiario?>(null) }
                    var estaEscribiendo by remember { mutableStateOf(false) }

                    AnimatedContent(
                        targetState = esPrimerInicioState,
                        transitionSpec = {
                            if (initialState == true && targetState == false) {
                                // Transición cinemática de salida de BienvenidaScreen -> Entrada a HomeScreen
                                (fadeIn(
                                    animationSpec = tween(durationMillis = 380, delayMillis = 60, easing = LinearOutSlowInEasing)
                                ) + scaleIn(
                                    initialScale = 0.96f,
                                    animationSpec = spring(
                                        dampingRatio = 0.85f,
                                        stiffness = 350f
                                    )
                                )) togetherWith (fadeOut(
                                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                ) + scaleOut(
                                    targetScale = 1.04f,
                                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                ))
                            } else {
                                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                            }
                        },
                        label = "OnboardingTransition"
                    ) { esPrimerInicio ->
                        when (esPrimerInicio) {
                            null -> {
                                Box(modifier = Modifier.fillMaxSize().background(FondoSistemaLight))
                            }
                            true -> {
                                BienvenidaScreen(
                                    onContinuar = { fotoUri ->
                                        coroutineScope.launch {
                                            if (!fotoUri.isNullOrBlank()) {
                                                preferencesManager.guardarFotoPortadaUri(fotoUri)
                                            }
                                            preferencesManager.setEsPrimerInicio(false)
                                        }
                                    }
                                )
                            }
                            false -> {
                                val escalaFondo by animateFloatAsState(
                                    targetValue = if (estaEscribiendo) 0.93f else 1f,
                                    animationSpec = IosMotion.SheetSpring,
                                    label = "EscalaFondo"
                                )
                                val radioEsquinas by animateDpAsState(
                                    targetValue = if (estaEscribiendo) 32.dp else 0.dp,
                                    animationSpec = spring(stiffness = 400f),
                                    label = "RadioFondo"
                                )
                                val dimAlpha by animateFloatAsState(
                                    targetValue = if (estaEscribiendo) 0.45f else 0f,
                                    animationSpec = IosMotion.SheetSpring,
                                    label = "DimAlpha"
                                )

                                val transicionY by animateFloatAsState(
                                    targetValue = if (estaEscribiendo) 0f else 2000f,
                                    animationSpec = IosMotion.SheetSpring,
                                    label = "SheetSlide"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black)
                                ) {
                                    // Capa 1: Fondo Retráctil Optimizado en GPU a 120 FPS mediante RenderNode directo
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = escalaFondo
                                                scaleY = escalaFondo
                                                shape = RoundedCornerShape(radioEsquinas)
                                                clip = true
                                                cameraDistance = 12f * density
                                            }
                                            .background(MaterialTheme.colorScheme.background)
                                    ) {
                                        when (pantallaActual) {
                                            "home" -> {
                                                HomeScreen(
                                                    viewModel = viewModel,
                                                    preferencesManager = preferencesManager,
                                                    fotoPortadaUri = fotoPortadaUriState ?: "",
                                                    temaPreferido = temaActual ?: "sistema",
                                                    onNavigateToEscribir = {
                                                        notaSeleccionada = null
                                                        estaEscribiendo = true
                                                    },
                                                    onEntradaClick = { entrada ->
                                                        notaSeleccionada = entrada
                                                        pantallaActual = "ver"
                                                    }
                                                )
                                            }
                                            "ver" -> {
                                                if (notaSeleccionada != null) {
                                                    VerEntradaScreen(
                                                        entrada = notaSeleccionada!!,
                                                        viewModel = viewModel,
                                                        onNavigateBack = {
                                                            notaSeleccionada = null
                                                            pantallaActual = "home"
                                                        },
                                                        onEditarClick = { entrada ->
                                                            notaSeleccionada = entrada
                                                            estaEscribiendo = true
                                                        }
                                                    )
                                                } else {
                                                    pantallaActual = "home"
                                                }
                                            }
                                        }

                                        // Velo oscuro de atenuación semitransparente
                                        if (dimAlpha > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = dimAlpha))
                                                    .clickable {
                                                        estaEscribiendo = false
                                                    }
                                            )
                                        }
                                    }

                                    // Capa 2: Hoja Superior Modal Pre-calentada en GPU (EscribirEntradaScreen)
                                    if (transicionY < 1950f) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(top = 10.dp)
                                                .graphicsLayer {
                                                    translationY = transicionY
                                                    alpha = if (transicionY > 1800f) 0f else 1f
                                                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                                                    clip = true
                                                },
                                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                            color = MaterialTheme.colorScheme.background
                                        ) {
                                            EscribirEntradaScreen(
                                                viewModel = viewModel,
                                                entradaParaEditar = notaSeleccionada,
                                                onNavigateBack = {
                                                    notaSeleccionada = null
                                                    estaEscribiendo = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
