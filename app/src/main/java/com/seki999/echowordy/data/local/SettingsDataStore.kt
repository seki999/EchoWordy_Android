package com.seki999.echowordy.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.seki999.echowordy.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "echowordy_settings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private object Keys {
        val CARD_DURATION_MS = longPreferencesKey("card_duration_ms")
        val SELECTED_VOICE_NAME = stringPreferencesKey("selected_voice_name")
    }

    override fun cardDurationMsFlow(): Flow<Long> =
        context.settingsDataStore.data.map { prefs ->
            prefs[Keys.CARD_DURATION_MS] ?: SettingsRepository.DEFAULT_CARD_DURATION_MS
        }

    override suspend fun setCardDurationMs(durationMs: Long) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.CARD_DURATION_MS] = durationMs
        }
    }

    override fun selectedVoiceNameFlow(): Flow<String?> =
        context.settingsDataStore.data.map { prefs -> prefs[Keys.SELECTED_VOICE_NAME] }

    override suspend fun setSelectedVoiceName(voiceName: String?) {
        context.settingsDataStore.edit { prefs ->
            if (voiceName == null) {
                prefs.remove(Keys.SELECTED_VOICE_NAME)
            } else {
                prefs[Keys.SELECTED_VOICE_NAME] = voiceName
            }
        }
    }
}
