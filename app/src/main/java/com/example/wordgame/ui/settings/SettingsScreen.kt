package com.example.wordgame.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wordgame.presentation.settings.SettingsUiState

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.displayMedium)
        Text("Best Score: ${state.bestScore}")
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            label = { Text("Player Name") },
            modifier = Modifier.fillMaxWidth()
        )
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        Button(onClick = onSave, enabled = !state.isSaving, modifier = Modifier.align(Alignment.End)) {
            Text(if (state.isSaving) "Saving..." else "Save")
        }
        Button(onClick = onReset, modifier = Modifier.align(Alignment.Start)) {
            Text("Reset Progress")
        }
    }
}
