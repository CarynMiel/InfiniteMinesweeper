package com.miel.minesweeper

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform