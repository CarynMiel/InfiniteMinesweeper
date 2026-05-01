package com.miel.minesweeper.core

import kotlin.math.abs

class Game (key: Long, density: Double){
    // hidden and shown data
    val hidden = Board(key, density)
    val shown = HashMap<Long, Char>()

    // the max size of the board
    var minX: Int = -1
    var minY: Int = -1
    var maxX: Int = 1
    var maxY: Int = 1

    // save state variables
    var flags = 0
    var score = 0

    fun flag(key: Long) {
        if(shown[key] == null || shown[key] == Tile.UNKNOWN) {
            shown[key] = Tile.FLAG
            flags++
        } else if(shown[key] == Tile.FLAG) {
            shown[key] = Tile.UNKNOWN
            flags--
        }
    }

    fun flag(x: Int, y: Int) {
        flag(Tile.key(x, y))
    }

    fun isFlagged(key: Long): Boolean {
        return shown[key] == Tile.FLAG
    }

    fun isFlagged(x: Int, y: Int): Boolean {
        return shown[Tile.key(x, y)] == Tile.FLAG
    }

    fun flagCount(key: Long): Int {
        var count = 0
        val around = Tile.keysAround(key)
        for(tile in around) {
            if(isFlagged(tile) || shown[tile] == Tile.BOMB) {count++}
        }; return count
    }

    fun flagCount(x: Int, y: Int): Int {
        return flagCount(Tile.key(x, y))
    }

    fun open(key: Long) {
        val queue = ArrayDeque<Long>()
        queue.addLast(key)

        // if the number matches the flags count
        if(shown[queue.first()] != null) {
            val digit: Boolean = shown[queue.first()]!!.isDigit()
            val match = flagCount(key).digitToChar() == hidden.valueAt(key)
            if(digit && match) {
                val around: Array<Long> = Tile.keysAround(queue.first())
                for(tile in around) {
                    queue.addLast(tile)
                }
            }
        }

        // flood open for blank tiles
        while(queue.isNotEmpty()) {
            val keyNow = queue.removeFirst()

            // if the tile already exists and is not unknown
            if(shown[keyNow] != null && shown[keyNow] != Tile.UNKNOWN) {
                continue
            }; shown[keyNow] = hidden.valueAt(keyNow)

            if(shown[keyNow]!!.isDigit()) {
                score += shown[keyNow]!!.digitToInt()
            } else if(shown[keyNow]!! == Tile.BOMB) {
                score -= 100
            }





            // updates the minimum and maximum
            if(Tile.x(keyNow) < minX) {minX = Tile.x(keyNow)}
            else if(Tile.x(keyNow) > maxX) {maxX = Tile.x(keyNow)}
            if(Tile.y(keyNow) < minY) {minY = Tile.y(keyNow)}
            else if(Tile.y(keyNow) > maxY) {maxY = Tile.y(keyNow)}

            // opening a blank tile and adding more to queue
            if(hidden.valueAt(keyNow) == Tile.BLANK) {
                val around: Array<Long> = Tile.keysAround(keyNow)
                for(tile in around) {
                    queue.addLast(tile)
                }
            }
        }
    }

    fun open(x: Int, y: Int) {
        open(Tile.key(x, y))
    }

    private fun boardLabel(): String {
        var text = "  "

        for(x in minX..maxX) {
            text += if(x % 10 == 0) {
                abs(x / 10).toString() + " "
            } else {
                abs(x % 10).toString() + " "
            }
        }; text += "\n"

        return text
    }

    fun seeHidden(): String {
        var text = boardLabel()

        for(y in minY..maxY) {
            text += if(y % 10 == 0) {
                abs(y / 10).toString() + " "
            } else {
                abs(y % 10).toString() + " "
            }
            for(x in minX..maxX) {
                text += hidden.valueAt(x, y) + " "
            }; text += "\n"
        }

        return text
    }

    fun seeShown(): String {
        var text = boardLabel()

        for(y in minY..maxY) {
            text += if(y % 10 == 0) {
                abs(y / 10).toString() + " "
            } else {
                abs(y % 10).toString() + " "
            }
            for(x in minX..maxX) {
                text += if(shown[Tile.key(x, y)] == null) {
                    Tile.UNKNOWN + " "
                } else {
                    shown[Tile.key(x, y)].toString() + " "
                }
            }; text += "\n"
        }
        return text
    }
}