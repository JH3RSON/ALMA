package com.ima.alma

import android.app.Application
import com.ima.alma.model.AlmaDatabase
import com.ima.alma.repository.DiarioRepository
import com.ima.alma.util.GestorCitasMazo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlmaApplication : Application() {
    val database by lazy { AlmaDatabase.getDatabase(this) }
    val repository by lazy { DiarioRepository(database.diarioDao()) }

    override fun onCreate() {
        super.onCreate()
        // Carga e inicialización del mazo de 1000+ citas en un hilo de fondo IO sin bloquear la interfaz
        CoroutineScope(Dispatchers.IO).launch {
            GestorCitasMazo.inicializar(this@AlmaApplication)
        }
    }
}
