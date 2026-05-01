package com.miel.minesweeper.core

class Tile {
    companion object {
        const val BLANK: Char = '0'
        const val BOMB: Char = 'X'
        const val FLAG: Char = 'F'
        const val UNKNOWN: Char = '?'

        fun tilesAround(globalX: Int, globalY: Int): Array<Array<Int>> {
            val around = arrayOf(
                arrayOf(globalX - 1, globalY - 1),
                arrayOf(globalX - 1, globalY),
                arrayOf(globalX - 1, globalY + 1),
                arrayOf(globalX, globalY - 1),
                arrayOf(globalX, globalY + 1),
                arrayOf(globalX + 1, globalY - 1),
                arrayOf(globalX + 1, globalY),
                arrayOf(globalX + 1, globalY + 1)
            )

            return around
        }

        fun tilesAround(key: Long): Array<Array<Int>> {
            val x = x(key)
            val y = y(key)
            return tilesAround(x, y)
        }

        fun keysAround(globalX: Int, globalY: Int): Array<Long> {
            val around = arrayOf(
                key(globalX - 1, globalY - 1),
                key(globalX - 1, globalY),
                key(globalX - 1, globalY + 1),
                key(globalX, globalY - 1),
                key(globalX, globalY + 1),
                key(globalX + 1, globalY - 1),
                key(globalX + 1, globalY),
                key(globalX + 1, globalY + 1),
            )
            return around
        }

        fun keysAround(key: Long): Array<Long> {
            val x = x(key)
            val y = y(key)
            return keysAround(x, y)
        }

        fun key(globalX: Int, globalY: Int): Long {
            val longX = globalX.toLong().shl(32)
            val longY = globalY.toLong() and 0x00000000ffffffffL
            return longX or longY
        }

        fun x(key: Long): Int {
            val num = key.shr(32)
            return num.toInt()
        }

        fun y(key: Long): Int {
            val num = key and 0x00000000ffffffffL
            return num.toInt()
        }
    }
}