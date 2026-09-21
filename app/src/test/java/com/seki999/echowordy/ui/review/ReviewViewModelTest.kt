package com.seki999.echowordy.ui.review

import com.seki999.echowordy.MainDispatcherRule
import com.seki999.echowordy.fakes.FakeSettingsRepository
import com.seki999.echowordy.fakes.FakeTtsController
import com.seki999.echowordy.fakes.FakeVocabularyRepository
import com.seki999.echowordy.tts.TtsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val DURATION_MS = 1000L

/**
 * A single card step is `advanceTimeBy(DURATION_MS); runCurrent()`: advancing
 * the clock schedules the due task without running it, and `runCurrent` runs
 * only what is due *right now* -- not tasks the ViewModel reschedules after
 * that. [advanceUntilIdle] is only used before the review has started (to let
 * the ViewModel's initial load finish) or after it has completed (to drain
 * the one-shot "create unknown words list" coroutine) -- never while
 * auto-play is active, since the ViewModel keeps rescheduling itself and
 * advanceUntilIdle would otherwise run the whole review to completion.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        repository: FakeVocabularyRepository,
        listId: Long,
        settings: FakeSettingsRepository = FakeSettingsRepository(DURATION_MS),
        tts: FakeTtsController = FakeTtsController(),
    ) = ReviewViewModel(repository, settings, tts, listId)

    @Test
    fun `first card is shown and spoken once loading completes`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "body1", "beta" to "body2"))
        val tts = FakeTtsController()
        val vm = createViewModel(repo, listId, tts = tts)

        advanceUntilIdle()
        vm.startReview()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.currentIndex)
        assertTrue(state.isPlaying)
        assertEquals(listOf("alpha"), tts.spokenWords)
    }

    @Test
    fun `auto-advances to the next card after the configured duration`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to "", "gamma" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()

        advanceTimeBy(DURATION_MS)
        runCurrent()

        assertEquals(1, vm.uiState.value.currentIndex)
    }

    @Test
    fun `review completes normally after the final card's duration elapses`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()
        advanceTimeBy(DURATION_MS)
        runCurrent()
        advanceTimeBy(DURATION_MS)
        runCurrent()

        val state = vm.uiState.value
        assertTrue(state.isCompleted)
        assertFalse(state.wasStopped)
        assertEquals(2, state.reviewedCardCount)
    }

    @Test
    fun `previous moves back one card and never goes before the first`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to "", "gamma" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()
        advanceTimeBy(DURATION_MS)
        runCurrent()
        assertEquals(1, vm.uiState.value.currentIndex)

        vm.previous()
        assertEquals(0, vm.uiState.value.currentIndex)

        vm.previous()
        assertEquals(0, vm.uiState.value.currentIndex)
    }

    @Test
    fun `next on the final card finishes the review`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()

        vm.next()
        assertEquals(1, vm.uiState.value.currentIndex)
        assertFalse(vm.uiState.value.isCompleted)

        vm.next()
        assertTrue(vm.uiState.value.isCompleted)
        assertFalse(vm.uiState.value.wasStopped)
    }

    @Test
    fun `pause stops auto-advance and resume continues it`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to "", "gamma" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()
        vm.pause()

        advanceTimeBy(DURATION_MS * 5)
        assertEquals(0, vm.uiState.value.currentIndex)
        assertTrue(vm.uiState.value.isPaused)

        vm.resume()
        assertFalse(vm.uiState.value.isPaused)

        advanceTimeBy(DURATION_MS)
        runCurrent()
        assertEquals(1, vm.uiState.value.currentIndex)
    }

    @Test
    fun `next and previous while paused move cards but do not resume auto-play`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to "", "gamma" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()
        vm.pause()

        vm.next()
        assertEquals(1, vm.uiState.value.currentIndex)
        assertTrue(vm.uiState.value.isPaused)

        advanceTimeBy(DURATION_MS * 5)
        assertEquals(1, vm.uiState.value.currentIndex)
    }

    @Test
    fun `stop ends the session immediately and records how many cards were reviewed`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList(
            "List A",
            listOf("alpha" to "", "beta" to "", "gamma" to "", "delta" to "", "epsilon" to ""),
        )
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()
        advanceTimeBy(DURATION_MS)
        runCurrent()

        vm.requestStop()
        assertTrue(vm.uiState.value.showStopConfirmation)

        vm.confirmStop()
        val state = vm.uiState.value
        assertTrue(state.isCompleted)
        assertTrue(state.wasStopped)
        assertEquals(2, state.reviewedCardCount)
    }

    @Test
    fun `cancelling the stop dialog keeps the review running`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()
        vm.requestStop()
        vm.cancelStop()

        val state = vm.uiState.value
        assertFalse(state.showStopConfirmation)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `marking a card unknown twice does not duplicate it, and it can be removed`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()

        vm.markCurrentAsUnknown()
        vm.markCurrentAsUnknown()
        assertEquals(1, vm.uiState.value.unknownCount)
        assertTrue(vm.uiState.value.isCurrentMarkedUnknown)

        vm.removeCurrentFromUnknown()
        assertEquals(0, vm.uiState.value.unknownCount)
        assertFalse(vm.uiState.value.isCurrentMarkedUnknown)
    }

    @Test
    fun `finishing with zero unknown words does not create a new list`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("Source", listOf("alpha" to "", "beta" to ""))
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()
        advanceTimeBy(DURATION_MS)
        runCurrent()
        advanceTimeBy(DURATION_MS)
        runCurrent()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isCompleted)
        assertEquals(0, state.unknownCount)
        assertNull(state.generatedUnknownListName)
        assertEquals(listOf("Source"), repo.getAllListNames())
    }

    @Test
    fun `generated unknown list preserves original card order and full body text`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList(
            "Source",
            listOf(
                "alpha" to "body-alpha",
                "beta" to "body-beta",
                "gamma" to "body-gamma",
            ),
        )
        val vm = createViewModel(repo, listId)

        advanceUntilIdle()
        vm.startReview()

        // Mark alpha (first) then, after auto-advancing twice, mark gamma (last).
        vm.markCurrentAsUnknown()
        advanceTimeBy(DURATION_MS)
        runCurrent()
        advanceTimeBy(DURATION_MS)
        runCurrent()
        vm.markCurrentAsUnknown()
        advanceTimeBy(DURATION_MS)
        runCurrent()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isCompleted)
        assertEquals(2, state.unknownCount)
        assertEquals("Source - Unknown Words", state.generatedUnknownListName)
        assertNotNull(state.generatedUnknownListId)

        val newCards = repo.getCardsOnce(state.generatedUnknownListId!!)
        assertEquals(listOf("alpha", "gamma"), newCards.map { it.word })
        assertEquals("body-alpha", newCards[0].body)
        assertEquals("body-gamma", newCards[1].body)
    }

    @Test
    fun `tts being unavailable is reflected in ui state without blocking playback`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeVocabularyRepository()
        val listId = repo.seedList("List A", listOf("alpha" to "", "beta" to ""))
        val tts = FakeTtsController(initialState = TtsState.UNAVAILABLE)
        val vm = createViewModel(repo, listId, tts = tts)

        advanceUntilIdle()
        assertTrue(vm.uiState.value.ttsUnavailable)

        vm.startReview()
        assertEquals(0, vm.uiState.value.currentIndex)
        assertTrue(vm.uiState.value.isPlaying)
    }
}
