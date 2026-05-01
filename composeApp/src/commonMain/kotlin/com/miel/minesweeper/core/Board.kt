package com.miel.minesweeper.core

class Board (val seed: Long, val density: Double){
    // procedural generation constants
    val primeX: Long = 80837813181374309L
    val primeY: Long = 4879324427L
    val start: Long = 37341137515395871L
    val scramble: Long = 1140071481932319845L

    // methods
    fun isBomb(globalX: Int, globalY: Int): Boolean {
        var hash: Long = start
        hash = hash xor seed
        hash = hash xor (primeX * globalX.toLong())
        hash = hash xor (primeY + globalY.toLong())
        hash *= scramble

        var value: Double = (hash and Long.MAX_VALUE).toDouble()
        value /= Long.MAX_VALUE.toDouble()
        return value < density
    }

    fun isBomb(key: Long): Boolean {
        val x = Tile.x(key)
        val y = Tile.y(key)
        return isBomb(x, y)
    }

    fun valueAt(globalX: Int, globalY: Int): Char {
        if(isBomb(globalX, globalY)) {return Tile.BOMB}
        var count = 0

        for(tile in Tile.tilesAround(globalX, globalY)) {
            if(isBomb(tile[0], tile[1])) {count++}
        }

        if(count == 0) {return Tile.BLANK}
        return count.digitToChar()
    }

    fun valueAt(key: Long): Char {
        val x = Tile.x(key)
        val y = Tile.y(key)
        return valueAt(x, y)
    }
}