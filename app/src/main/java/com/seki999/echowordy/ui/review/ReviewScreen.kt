package com.seki999.echowordy.ui.review

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.HorizontalDivider
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
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = uiState.listName, style = MaterialTheme.typography.titleLarge)
            Text(text = uiState.progressText, style = MaterialTheme.typography.titleLarge)
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
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = uiState.currentCard?.word.orEmpty(),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = uiState.currentCard?.body.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (uiState.isCurrentMarkedUnknown) {
            OutlinedButton(
                onClick = viewModel::removeCurrentFromUnknown,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp),
            ) {
                Text("Marked as Unknown — Remove from Unknown")
            }
        } else {
            OutlinedButton(
                onClick = viewModel::markCurrentAsUnknown,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp),
            ) {
                Text("Mark as Unknown")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = viewModel::previous,
                enabled = uiState.currentIndex > 0,
                modifier = Modifier.weight(1f).height(56.dp),
            ) {
                Text("Previous")
            }
            Button(
                onClick = { if (uiState.isPaused) viewModel.resume() else viewModel.pause() },
                modifier = Modifier.weight(1f).height(56.dp),
            ) {
                Text(if (uiState.isPaused) "Resume" else "Pause")
            }
            OutlinedButton(
                onClick = viewModel::next,
                modifier = Modifier.weight(1f).height(56.dp),
            ) {
                Text("Next")
            }
        }

        OutlinedButton(
            onClick = viewModel::requestStop,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(56.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text("Stop")
        }
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
            .padding(24.dp),
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text("Open Unknown Words")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = onBackToLists,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("Back to Lists")
        }
    }
}
