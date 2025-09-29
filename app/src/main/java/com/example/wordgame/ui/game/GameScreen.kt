package com.example.wordgame.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wordgame.domain.model.ClueType
import com.example.wordgame.domain.model.GameStatus
import com.example.wordgame.presentation.game.GameUiState

@Composable
fun GameScreen(
    state: GameUiState,
    onGuessChanged: (String) -> Unit,
    onSubmitGuess: () -> Unit,
    onRequestClue: (ClueType) -> Unit,
    onNextLevel: () -> Unit,
    onRetryLevel: () -> Unit,
    onSubmitScore: () -> Unit,
    onQuitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val feedback = state.message
        if (!feedback.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = feedback)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HeaderStats(state)
                WordCard(state)
                GuessSection(state, onGuessChanged, onSubmitGuess)
                ClueActions(state, onRequestClue)
                // Display clue history instead of single clue message
                if (state.clueHistory.isNotEmpty()) {
                    ClueHistory(state.clueHistory.map { "-${it.cost} pts: ${it.message}" })
                }
                OutcomeActions(state, onNextLevel, onRetryLevel, onSubmitScore)
                OutlinedButton(
                    onClick = onQuitGame,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quit Game")
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun HeaderStats(state: GameUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (state.playerName.isBlank()) "Guest player" else state.playerName,
                style = MaterialTheme.typography.titleLarge
            )
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                StatBadge(label = "Level", value = state.level.toString())
                StatBadge(label = "Score", value = state.score.toString())
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                StatBadge(label = "Attempts", value = state.attemptsLeft.toString())
                StatBadge(label = "Time", value = "${state.elapsedSeconds}s")
            }
            StatBadge(label = "Best", value = state.bestScore.toString())
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun WordCard(state: GameUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Guess the word", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.obscuredWord.isBlank()) "_ _ _" else state.obscuredWord,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GuessSection(
    state: GameUiState,
    onGuessChanged: (String) -> Unit,
    onSubmitGuess: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Your guess", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.currentGuess,
                onValueChange = onGuessChanged,
                label = { Text("Type your word") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmitGuess() }),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onSubmitGuess,
                enabled = state.status == GameStatus.PLAYING,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Guess")
            }
        }
    }
}

@Composable
private fun ClueActions(
    state: GameUiState,
    onRequestClue: (ClueType) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Clues", style = MaterialTheme.typography.titleMedium)
            Text(text = "Spend 5 points to reveal helpful hints.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ClueChip(
                    label = "Word length",
                    enabled = state.status == GameStatus.PLAYING && !state.usedClues.contains(ClueType.LETTER_COUNT),
                    onClick = { onRequestClue(ClueType.LETTER_COUNT) }
                )
                ClueChip(
                    label = "Letter count",
                    enabled = state.status == GameStatus.PLAYING && !state.usedClues.contains(ClueType.LETTER_OCCURRENCE),
                    onClick = { onRequestClue(ClueType.LETTER_OCCURRENCE) }
                )
            }
            ClueChip(
                label = "Word tip",
                enabled = state.status == GameStatus.PLAYING && !state.usedClues.contains(ClueType.WORD_TIP),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onRequestClue(ClueType.WORD_TIP) }
            )
        }
    }
}

@Composable
private fun ClueHistory(messages: List<String>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Hints so far", style = MaterialTheme.typography.titleMedium)
            messages.forEach { hint ->
                Text(hint, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ClueChip(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun OutcomeActions(
    state: GameUiState,
    onNextLevel: () -> Unit,
    onRetryLevel: () -> Unit,
    onSubmitScore: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state.status) {
                GameStatus.WON -> {
                    Text("Great job! Advance for a tougher challenge.")
                    ElevatedButton(onClick = onNextLevel, modifier = Modifier.fillMaxWidth()) {
                        Text("Next Level")
                    }
                    Button(
                        onClick = onSubmitScore,
                        enabled = !state.isSubmittingScore,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isSubmittingScore) "Submitting..." else "Submit Score Online")
                    }
                }
                GameStatus.LOST -> {
                    Text("Out of attempts. Try again!")
                    ElevatedButton(onClick = onRetryLevel, modifier = Modifier.fillMaxWidth()) {
                        Text("Retry Level")
                    }
                }
                GameStatus.PLAYING -> {
                    TextButton(onClick = onRetryLevel, modifier = Modifier.align(Alignment.End)) {
                        Text("Restart Round")
                    }
                }
            }
        }
    }
}