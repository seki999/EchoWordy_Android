package com.seki999.echowordy.ui.review

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import com.seki999.echowordy.ui.theme.LocalReadingPreferences
import com.seki999.echowordy.ui.theme.ReadingDimensions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seki999.echowordy.ui.util.KeepScreenOn

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBackToLists: () -> Unit,
    onOpenUnknownList: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    KeepScreenOn()

    BackHandler(enabled = !uiState.isCompleted) {
        viewModel.requestStop()
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            viewModel.startReview()
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> LoadingContent()
                uiState.isCompleted -> ReviewResultContent(
                    uiState = uiState,
                    onOpenUnknownList = onOpenUnknownList,
                    onBackToLists = onBackToLists,
                )
                else -> ReviewPlaybackContent(uiState = uiState, viewModel = viewModel)
            }
        }
    }

    if (uiState.showStopConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::cancelStop,
            title = { Text("Stop Review?") },
            text = { Text("Your marked unknown words will be saved.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmStop) { Text("Stop") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelStop) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ReviewPlaybackContent(uiState: ReviewUiState, viewModel: ReviewViewModel) {
    val scale = LocalReadingPreferences.current.fontSize.scale
    val scroll = rememberScrollState()
    LaunchedEffect(uiState.currentCard?.id) { scroll.scrollTo(0) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val horizontal = if (maxWidth >= 420.dp) ReadingDimensions.WideScreenHorizontalPadding else ReadingDimensions.ScreenHorizontalPadding
        val controlMeasurer = rememberTextMeasurer()
        val labelWidth = with(LocalDensity.current) {
            controlMeasurer.measure("Previous", TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)).size.width.toDp()
        }
        val stacked = (maxWidth - horizontal * 2 - 16.dp) / 3 < labelWidth + 16.dp || maxHeight < 400.dp
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = horizontal, vertical = 12.dp)
            .then(if (stacked) Modifier.verticalScroll(scroll) else Modifier)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = uiState.listName, modifier = Modifier.weight(1f).padding(end = 12.dp), style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = uiState.progressText, style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (uiState.ttsUnavailable) {
                Text(
                    text = "American English voice is unavailable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Column(
                modifier = Modifier
                    .then(if (stacked) Modifier else Modifier.weight(1f).verticalScroll(scroll))
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(bottom = 24.dp),
            ) {
                FittingWord(uiState.currentCard?.word.orEmpty(), (ReadingDimensions.WordTitleSize.value * scale).coerceAtMost(ReadingDimensions.WordTitleMax.value))
                Spacer(Modifier.height(ReadingDimensions.WordToIpa))
                val blocks = remember(uiState.currentCard?.body) { readingBlocks(uiState.currentCard?.body.orEmpty()) }
                blocks.forEachIndexed { index, block ->
                    val size = when(block.role) {
                        ReadingRole.IPA -> ReadingDimensions.IpaTextSize
                        ReadingRole.MEANING -> ReadingDimensions.MeaningTextSize
                        ReadingRole.COLLOCATION -> ReadingDimensions.CollocationTextSize
                        ReadingRole.TRANSLATION -> ReadingDimensions.CollocationChineseSize
                        ReadingRole.CHINESE_EXAMPLE -> ReadingDimensions.ExampleChineseSize
                        else -> ReadingDimensions.ExampleEnglishSize
                    } * scale
                    Text(block.text, fontSize = size, lineHeight = size * ReadingDimensions.ReadingLineSpacing,
                        fontWeight = if (block.role == ReadingRole.MEANING) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (block.role == ReadingRole.IPA || block.role == ReadingRole.TRANSLATION)
                            MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    if (index < blocks.lastIndex) Spacer(Modifier.height(when(block.role) {
                        ReadingRole.IPA -> ReadingDimensions.IpaToMeaning
                        ReadingRole.CHINESE_EXAMPLE -> ReadingDimensions.ExampleSpacing
                        else -> ReadingDimensions.SectionSpacing
                    }))
                }
            }

            if (uiState.isCurrentMarkedUnknown) {
                OutlinedButton(
                    onClick = viewModel::removeCurrentFromUnknown,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = ReadingDimensions.ControlHeight),
                ) {
                    Text("Marked as Unknown — Remove from Unknown")
                }
            } else {
                OutlinedButton(
                    onClick = viewModel::markCurrentAsUnknown,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = ReadingDimensions.ControlHeight),
                ) {
                    Text("Mark as Unknown")
                }
            }

            val controls: @Composable (Modifier) -> Unit = { modifier ->
                OutlinedButton(onClick = viewModel::previous, enabled = uiState.currentIndex > 0,
                    modifier = modifier.heightIn(min = ReadingDimensions.ControlHeight),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp)) {
                    Text("Previous", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, maxLines = 1, softWrap = false)
                }
                Button(onClick = { if (uiState.isPaused) viewModel.resume() else viewModel.pause() },
                    modifier = modifier.heightIn(min = ReadingDimensions.ControlHeight),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp)) {
                    Text(if (uiState.isPaused) "Play" else "Pause", fontSize = 20.sp, maxLines = 1, softWrap = false)
                }
                OutlinedButton(onClick = viewModel::next, modifier = modifier.heightIn(min = ReadingDimensions.ControlHeight),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp)) {
                    Text("Next", fontSize = 18.sp, maxLines = 1, softWrap = false)
                }
            }
            if (stacked) Column(Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                controls(Modifier.fillMaxWidth())
            } else Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                controls(Modifier.weight(1f))
            }

            OutlinedButton(
                onClick = viewModel::requestStop,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = ReadingDimensions.ControlHeight),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Stop")
            }
        }
    }
}

@Composable
private fun FittingWord(word: String, initialSize: Float) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = with(LocalDensity.current) { maxWidth.roundToPx() }
        var size = initialSize
        while (size > 16f && measurer.measure(word, TextStyle(fontSize = size.sp, fontWeight = FontWeight.Bold), softWrap = false).size.width > width) size -= 1f
        Text(word, fontSize = size.sp, lineHeight = (size * 1.2f).sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ReviewResultContent(
    uiState: ReviewUiState,
    onOpenUnknownList: (Long) -> Unit,
    onBackToLists: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (uiState.wasStopped) "Review Stopped" else "Review Complete",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "${uiState.reviewedCardCount} cards reviewed",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "${uiState.unknownCount} unknown words",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            uiState.unknownCount == 0 -> {
                Text(
                    text = "No unknown words were marked.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                )
            }
            uiState.isAwaitingUnknownListCreation -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(end = 8.dp))
                    Text("Creating Unknown Words list…")
                }
            }
            else -> {
                Text(
                    text = "A new list has been created:",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = uiState.generatedUnknownListName.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.generatedUnknownListId != null) {
            Button(
                onClick = { onOpenUnknownList(uiState.generatedUnknownListId) },
                modifier = Modifier.fillMaxWidth().heightIn(min = ReadingDimensions.ControlHeight),
            ) {
                Text("Open Unknown Words")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = onBackToLists,
            modifier = Modifier.fillMaxWidth().heightIn(min = ReadingDimensions.ControlHeight),
        ) {
            Text("Back to Lists")
        }
    }
}
