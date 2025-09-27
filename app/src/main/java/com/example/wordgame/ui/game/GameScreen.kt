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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderStats(state)
            Divider()
            WordCard(state)
            GuessSection(state, onGuessChanged, onSubmitGuess)
            ClueActions(state, onRequestClue)
            state.clueMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.secondary)
            }
            state.message?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            OutcomeActions(state, onNextLevel, onRetryLevel, onSubmitScore)
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun HeaderStats(state: GameUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Player: ${state.playerName.ifBlank { "Guest" }}", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Level ${state.level}")
            Text("Score ${state.score}")
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Attempts ${state.attemptsLeft}")
            Text("Time ${state.elapsedSeconds}s")
        }
        Text("Best: ${state.bestScore}")
    }
}

@Composable
private fun WordCard(state: GameUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = "Guess the word:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (state.obscuredWord.isBlank()) "_ _ _" else state.obscuredWord,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GuessSection(
    state: GameUiState,
    onGuessChanged: (String) -> Unit,
    onSubmitGuess: () -> Unit
) {
    OutlinedTextField(
        value = state.currentGuess,
        onValueChange = onGuessChanged,
        label = { Text("Your guess") },
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

@Composable
private fun ClueActions(
    state: GameUiState,
    onRequestClue: (ClueType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Need a clue? Each costs 5 points.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onRequestClue(ClueType.LETTER_COUNT) },
                enabled = state.status == GameStatus.PLAYING && !state.usedClues.contains(ClueType.LETTER_COUNT),
                modifier = Modifier.weight(1f)
            ) { Text("Word Length") }
            OutlinedButton(
                onClick = { onRequestClue(ClueType.LETTER_OCCURRENCE) },
                enabled = state.status == GameStatus.PLAYING && !state.usedClues.contains(ClueType.LETTER_OCCURRENCE),
                modifier = Modifier.weight(1f)
            ) { Text("Letter Count") }
        }
        OutlinedButton(
            onClick = { onRequestClue(ClueType.WORD_TIP) },
            enabled = state.status == GameStatus.PLAYING && !state.usedClues.contains(ClueType.WORD_TIP),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Word Tip") }
    }
}

@Composable
private fun OutcomeActions(
    state: GameUiState,
    onNextLevel: () -> Unit,
    onRetryLevel: () -> Unit,
    onSubmitScore: () -> Unit
) {
    when (state.status) {
        GameStatus.WON -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        }
        GameStatus.LOST -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Out of attempts. Try again!")
                ElevatedButton(onClick = onRetryLevel, modifier = Modifier.fillMaxWidth()) {
                    Text("Retry Level")
                }
            }
        }
        GameStatus.PLAYING -> {
            TextButton(onClick = onRetryLevel) {
                Text("Restart Round")
            }
        }
    }
}
