package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.core.crash.GlobalExceptionHandler
import com.example.presentation.ui.navigation.SaraNavHost
import com.example.ui.theme.SaraAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalExceptionHandler.initialize(this)
        com.example.core.voice.AndroidVoiceManager.init(this)
        enableEdgeToEdge()
        setContent {
            SaraAITheme {
                SaraNavHost()
            }
        }
    }
}

