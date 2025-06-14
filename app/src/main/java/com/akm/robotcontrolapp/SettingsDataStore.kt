package com.akm.robotcontrolapp

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    private val SERVER_IP_KEY = stringPreferencesKey("server_ip")

    suspend fun saveServerIp(ip: String) {
        context.dataStore.edit { settings ->
            settings[SERVER_IP_KEY] = ip
        }
    }

    suspend fun getServerIp(): String {
        return context.dataStore.data
            .map { prefs -> prefs[SERVER_IP_KEY] ?: "192.168.1.5" }
            .first()
    }
}
