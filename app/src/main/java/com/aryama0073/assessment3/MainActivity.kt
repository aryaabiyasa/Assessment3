package com.aryama0073.assessment3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aryama0073.assessment3.ui.screen.MainScreen
import com.aryama0073.assessment3.ui.theme.Assessment3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Assessment3Theme {
                MainScreen()
            }
        }
    }
}