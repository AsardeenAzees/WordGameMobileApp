package com.example.wordgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.wordgame.ui.WordGameApp
import com.example.wordgame.ui.theme.WordGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordGameTheme {
                Surface {
                    val container = remember { AppContainer(this@MainActivity) }
                    WordGameApp(container)
                }
            }
        }
    }
}
