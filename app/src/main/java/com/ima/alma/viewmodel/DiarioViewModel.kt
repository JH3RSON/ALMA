package com.ima.alma.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ima.alma.model.EntradaDiario
import com.ima.alma.repository.DiarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiarioViewModel(private val repository: DiarioRepository) : ViewModel() {

    // Suscripción puramente reactiva mediante StateFlow para cero bloqueos en el hilo principal
    val todasLasEntradas: StateFlow<List<EntradaDiario>> = repository.todasLasEntradas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertar(entrada: EntradaDiario) {
        viewModelScope.launch {
            repository.insertar(entrada)
        }
    }

    fun actualizar(entrada: EntradaDiario) {
        viewModelScope.launch {
            repository.actualizar(entrada)
        }
    }

    fun eliminar(entrada: EntradaDiario) {
        viewModelScope.launch {
            repository.eliminar(entrada)
        }
    }

    fun insertar(
        texto: String,
        titulo: String? = null,
        emojiAnimo: String = "neutral",
        fechaMilisegundos: Long = System.currentTimeMillis(),
        fotoUri: String? = null,
        hashtags: String? = null
    ) {
        viewModelScope.launch {
            repository.insertar(
                EntradaDiario(
                    titulo = titulo,
                    texto = texto,
                    emojiAnimo = emojiAnimo,
                    fechaMilisegundos = fechaMilisegundos,
                    fotoUri = fotoUri,
                    hashtags = hashtags
                )
            )
        }
    }
}

class DiarioViewModelFactory(private val repository: DiarioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiarioViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
