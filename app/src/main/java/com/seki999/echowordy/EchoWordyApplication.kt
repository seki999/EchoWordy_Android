package com.seki999.echowordy

import android.app.Application
import com.seki999.echowordy.data.local.AppDatabase
import com.seki999.echowordy.data.local.SettingsRepositoryImpl
import com.seki999.echowordy.data.repository.VocabularyRepositoryImpl
import com.seki999.echowordy.domain.repository.SettingsRepository
import com.seki999.echowordy.domain.repository.VocabularyRepository
import com.seki999.echowordy.tts.AndroidTtsController
import com.seki999.echowordy.tts.TtsController

/**
 * Simple hand-rolled dependency container. The app is small enough that a
 * DI framework would add more ceremony than value; every dependency here is
 * a cheap, lazily-created singleton for the process lifetime.
 */
class EchoWordyApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val vocabularyRepository: VocabularyRepository by lazy {
        VocabularyRepositoryImpl(database, database.vocabularyListDao(), database.vocabularyCardDao())
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(this) }

    val ttsController: TtsController by lazy { AndroidTtsController(this) }
}
