package com.seki999.echowordy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.seki999.echowordy.EchoWordyApplication
import com.seki999.echowordy.ui.edit.PasteCardsViewModel
import com.seki999.echowordy.ui.home.HomeViewModel
import com.seki999.echowordy.ui.listdetail.ListDetailViewModel
import com.seki999.echowordy.ui.review.ReviewViewModel
import com.seki999.echowordy.ui.settings.SettingsViewModel

/**
 * A single factory for every ViewModel in the app. [listId] is only used by
 * screens that are scoped to a specific list; it is ignored otherwise.
 */
class ViewModelFactory(
    private val app: EchoWordyApplication,
    private val listId: Long = -1L,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(app.vocabularyRepository) as T

            modelClass.isAssignableFrom(ListDetailViewModel::class.java) ->
                ListDetailViewModel(app.vocabularyRepository, listId) as T

            modelClass.isAssignableFrom(PasteCardsViewModel::class.java) ->
                PasteCardsViewModel(app.vocabularyRepository, listId) as T

            modelClass.isAssignableFrom(ReviewViewModel::class.java) ->
                ReviewViewModel(app.vocabularyRepository, app.settingsRepository, app.ttsController, listId) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(app.settingsRepository, app.ttsController) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
