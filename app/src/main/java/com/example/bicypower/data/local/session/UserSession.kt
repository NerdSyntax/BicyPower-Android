package com.example.bicypower.data.local.session
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class UserSession(private val context: Context) {
    private val kLoggedIn = booleanPreferencesKey("logged_in")
    private val kRole     = stringPreferencesKey("role") // ADMIN | STAFF | CLIENT | ""

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[kLoggedIn] ?: false }
    val role: Flow<String>        = context.dataStore.data.map { it[kRole] ?: "" }

    suspend fun setLoggedIn(role: String) {
        context.dataStore.edit { p ->
            p[kLoggedIn] = true
            p[kRole] = role
        }
    }

    suspend fun logout() {
        context.dataStore.edit { p ->
            p[kLoggedIn] = false
            p[kRole] = ""
        }
    }
}