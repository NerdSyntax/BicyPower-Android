package com.example.bicypower.data.local.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val STORE_NAME = "user_prefs"

// Extensión DataStore
val Context.dataStore by preferencesDataStore(name = STORE_NAME)

class UserPreferences(private val context: Context) {

    private val KEY_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val KEY_ROLE      = stringPreferencesKey("role")
    private val KEY_PHOTO     = stringPreferencesKey("photo_uri")
    private val KEY_NAME      = stringPreferencesKey("user_name")
    private val KEY_EMAIL     = stringPreferencesKey("user_email")

    /** Lectura (Flows) */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_LOGGED_IN] ?: false }
    val role: Flow<String>        = context.dataStore.data.map { it[KEY_ROLE] ?: "" }
    val photoUri: Flow<String?>   = context.dataStore.data.map { it[KEY_PHOTO] }
    val userName: Flow<String>    = context.dataStore.data.map { it[KEY_NAME] ?: "" }
    val userEmail: Flow<String>   = context.dataStore.data.map { it[KEY_EMAIL] ?: "" }

    /** Escritura */
    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { it[KEY_LOGGED_IN] = value }
    }
    suspend fun setRole(value: String) {
        context.dataStore.edit { it[KEY_ROLE] = value }
    }
    suspend fun setPhoto(value: String?) {
        context.dataStore.edit { prefs ->
            if (value == null) prefs.remove(KEY_PHOTO) else prefs[KEY_PHOTO] = value
        }
    }
    suspend fun setSession(loggedIn: Boolean, role: String) {
        context.dataStore.edit {
            it[KEY_LOGGED_IN] = loggedIn
            it[KEY_ROLE] = role
        }
    }
    suspend fun setIdentity(name: String, email: String) {
        context.dataStore.edit {
            it[KEY_NAME] = name
            it[KEY_EMAIL] = email
        }
    }

    /** Cerrar sesión (mantenemos foto para mejor UX) */
    suspend fun logout() {
        context.dataStore.edit {
            it[KEY_LOGGED_IN] = false
            it[KEY_ROLE] = ""
            it.remove(KEY_NAME)
            it.remove(KEY_EMAIL)
        }
    }
}
