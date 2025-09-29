package com.example.wordgame.ui.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wordgame.presentation.leaderboard.LeaderboardUiState

@Composable
fun LeaderboardScreen(
    state: LeaderboardUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Global Leaderboard",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(onClick = onRefresh) {
                Text("Refresh")
            }
        }

        if (state.isLoading && state.entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        when {
            state.entries.isEmpty() && !state.isLoading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No scores yet. Win a round to claim the top spot!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.entries) { index, entry ->
                        val resolvedRank = entry.rank ?: (index + 1)
                        val highlight = resolvedRank in 1..3
                        val containerColor = if (highlight) {
                            when (resolvedRank) {
                                1 -> MaterialTheme.colorScheme.primaryContainer // Gold for 1st
                                2 -> MaterialTheme.colorScheme.secondaryContainer // Silver for 2nd
                                3 -> MaterialTheme.colorScheme.tertiaryContainer // Bronze for 3rd
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                        val contentColor = if (highlight) {
                            when (resolvedRank) {
                                1 -> MaterialTheme.colorScheme.onPrimaryContainer
                                2 -> MaterialTheme.colorScheme.onSecondaryContainer
                                3 -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                        val timeDisplay = if (entry.timeSeconds == Int.MAX_VALUE) "?" else "${entry.timeSeconds}s"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank with better visibility
                                Text(
                                    text = when (resolvedRank) {
                                        1 -> "🏆 $resolvedRank"
                                        2 -> "🥈 $resolvedRank"
                                        3 -> "🥉 $resolvedRank"
                                        else -> "$resolvedRank."
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = contentColor,
                                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
                                )
                                
                                // Player name and score
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = entry.player,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = contentColor,
                                        fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "${entry.score} pts",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = contentColor.copy(alpha = 0.8f)
                                    )
                                }
                                
                                // Time with better formatting
                                Text(
                                    text = timeDisplay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}