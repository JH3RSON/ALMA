package com.ima.alma.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_ES_PRIMER_INICIO = booleanPreferencesKey("es_primer_inicio")
        val KEY_ES_PRIMERA_VEZ = booleanPreferencesKey("es_primera_vez")
        val KEY_FOTO_PORTADA_URI = stringPreferencesKey("foto_portada_uri")
        val KEY_TEMA_PREFERIDO = stringPreferencesKey("tema_preferido")
        val KEY_IDIOMA_PREFERIDO = stringPreferencesKey("idioma_preferido")
    }

    val esPrimerInicio: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ES_PRIMER_INICIO] ?: (preferences[KEY_ES_PRIMERA_VEZ] ?: true)
    }

    val esPrimeraVez: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ES_PRIMERA_VEZ] ?: true
    }

    val fotoPortadaUri: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_FOTO_PORTADA_URI] ?: ""
    }

    val temaPreferido: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_TEMA_PREFERIDO] ?: "sistema"
    }

    val idiomaPreferido: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_IDIOMA_PREFERIDO] ?: "es"
    }

    suspend fun setEsPrimerInicio(esPrimerInicio: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ES_PRIMER_INICIO] = esPrimerInicio
            preferences[KEY_ES_PRIMERA_VEZ] = esPrimerInicio
        }
    }

    suspend fun guardarTemaPreferido(tema: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TEMA_PREFERIDO] = tema
        }
    }

    suspend fun guardarIdiomaPreferido(idioma: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IDIOMA_PREFERIDO] = idioma
        }
    }

    suspend fun guardarFotoPortadaUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FOTO_PORTADA_URI] = uri
        }
    }

    suspend fun guardarFotoPortadaYCompletarOnboarding(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FOTO_PORTADA_URI] = uri
            preferences[KEY_ES_PRIMER_INICIO] = false
            preferences[KEY_ES_PRIMERA_VEZ] = false
        }
    }

    suspend fun setEsPrimeraVez(esPrimeraVez: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ES_PRIMER_INICIO] = esPrimeraVez
            preferences[KEY_ES_PRIMERA_VEZ] = esPrimeraVez
        }
    }
}
