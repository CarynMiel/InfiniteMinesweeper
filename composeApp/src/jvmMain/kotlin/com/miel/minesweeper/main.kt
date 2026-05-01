package com.miel.minesweeper

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Minesweeper_V6",
    ) {
        App()
    }
}