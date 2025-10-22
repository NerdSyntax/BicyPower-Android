package com.example.bicypower.data.local.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    private val photoKey = stringPreferencesKey("profile_photo_uri")

    val photoUri: Flow<String?> = context.dataStore.data.map { it[photoKey] }

    suspend fun setPhoto(uriString: String?) {
        context.dataStore.edit { prefs ->
            if (uriString.isNullOrBlank()) prefs.remove(photoKey) else prefs[photoKey] = uriString
        }
    }
}
