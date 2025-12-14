package com.example.bicypower.data.local.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class UserSession(private val context: Context) {

    private val kLoggedIn = booleanPreferencesKey("logged_in")
    private val kRole = stringPreferencesKey("role")
    private val kUserId = longPreferencesKey("user_id")
    private val kUserName = stringPreferencesKey("user_name")
    private val kUserEmail = stringPreferencesKey("user_email")

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[kLoggedIn] ?: false }
    val role: Flow<String> = context.dataStore.data.map { it[kRole] ?: "" }
    val userId: Flow<Long> = context.dataStore.data.map { it[kUserId] ?: 0L }
    val userName: Flow<String> = context.dataStore.data.map { it[kUserName] ?: "" }
    val userEmail: Flow<String> = context.dataStore.data.map { it[kUserEmail] ?: "" }

    /**
     * Método compatible con tu AuthViewModel (session.setSession(...))
     */
    suspend fun setSession(
        role: String,
        userId: Long,
        name: String,
        email: String
    ) {
        setLoggedIn(
            userId = userId,
            role = role,
            name = name,
            email = email
        )
    }

    suspend fun setLoggedIn(
        userId: Long,
        role: String,
        name: String,
        email: String
    ) {
        context.dataStore.edit { p ->
            p[kLoggedIn] = true
            p[kRole] = role
            p[kUserId] = userId
            p[kUserName] = name
            p[kUserEmail] = email
        }
    }

    suspend fun logout() {
        context.dataStore.edit { p ->
            p[kLoggedIn] = false
            p[kRole] = ""
            p[kUserId] = 0L
            p[kUserName] = ""
            p[kUserEmail] = ""
        }
    }
}
