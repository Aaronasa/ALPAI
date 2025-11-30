package com.example.alpai.routing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.alpai.enmuclass.Screen
import com.example.alpai.view.CameraLiveScreen
import com.example.alpai.view.MenuScreen
import com.example.alpai.view.StaticImageScreen
import com.example.alpai.viewmodel.ClassifierHelper

@Composable
fun AppRouting(classifier: ClassifierHelper) {
    // State untuk menentukan layar mana yang aktif
    var currentScreen by remember { mutableStateOf(Screen.MENU) }

    // Logika perpindahan layar
    when (currentScreen) {
        Screen.MENU -> {
            MenuScreen(
                onNavigateToLive = { currentScreen = Screen.LIVE_PREDICT },
                onNavigateToStatic = { currentScreen = Screen.STATIC_IMAGE }
            )
        }
        Screen.LIVE_PREDICT -> {
            CameraLiveScreen(
                classifier = classifier,
                onBack = { currentScreen = Screen.MENU }
            )
        }
        Screen.STATIC_IMAGE -> {
            StaticImageScreen(
                classifier = classifier,
                onBack = { currentScreen = Screen.MENU }
            )
        }
    }
}