package checker

import checker.Direction.*

enum class Direction(val rowOffset: Int, val colOffset: Int) {
    TOP_LEFT(-1, -1),
    TOP_RIGHT(-1, +1),
    BOTTOM_LEFT(+1, -1),
    BOTTOM_RIGHT(+1, +1)
}

class Checks {

    private var board = Array<StringBuilder>(8) {StringBuilder("")}

    private var xPoints: Int = 12
    private var oPoints: Int = 12

    private var isPlayerX: Boolean = true

    var gameWon: Boolean = false


    init {
        board[0] = StringBuilder("-o-o-o-o")
        board[1] = StringBuilder("o-o-o-o-")
        board[2] = StringBuilder("-o-o-o-o")

        board[3] = StringBuilder("+-+-+-+-")
        board[4] = StringBuilder("-+-+-+-+")

        board[5] = StringBuilder("x-x-x-x-")
        board[6] = StringBuilder("-x-x-x-x")
        board[7] = StringBuilder("x-x-x-x-")

        //testing...
        board[0] = StringBuilder("-o-o-+-o")
        board[1] = StringBuilder("o-o-o-o-")
        board[2] = StringBuilder("-o-+-o-+")
        board[3] = StringBuilder("+-o-o-o-")
        board[4] = StringBuilder("-x-+-+-+")
        board[5] = StringBuilder("+-x-x-x-")
        board[6] = StringBuilder("-x-x-x-x")
        board[7] = StringBuilder("x-x-x-x-")
    }

    fun printBoard() {
        println("  _1_2_3_4_5_6_7_8_")

        for (lineNum in board.indices)  {
            print("${lineNum+1} |")
            for (c in board[lineNum]) { //adding padding by printing char by char
                print("$c ")
            }
            println()
        }
    }


    fun move(row: Int, col: Int, dir: Direction) {

        println("trying to move from ${row+1} ${col+1} to the $dir")

        val playerChar = getCurrentPlayer()

        if (board[row][col].toString().toLowerCase() != playerChar) {
            println("No $playerChar at ${row+1} ${col+1}.")
            return
        }

        println("$playerChar currently at ${row+1} ${col+1}")

        if (canMove(row, col, dir)) {

            val alreadyKing = board[row][col].isUpperCase()

            //king making check
            //rules -> 1. reached enemy's first row. 2.enemy has captured pieces available to king them.
            if (!alreadyKing &&
                    isPlayerX && row + dir.rowOffset == 0 && xPoints < 12 ||
                    !isPlayerX && row + dir.rowOffset == 7 && oPoints < 12) {

                    replaceLine(row, col, dir, playerChar.toUpperCase())

                    if (isPlayerX) xPoints++ else oPoints ++

            } else { //continue as normal
                replaceLine(row, col, dir, board[row][col].toString())
            }

            println("moved to the $dir")

        } else if (canAttack(row, col, dir)) {
            attack(row, col, dir)
            println("attacked the $dir")

        } else {
            println("can't move to $dir from ${row+1} ${col+1}")
        }

    }

    private fun attack(row: Int, col: Int, dir: Direction) {
        if (canAttack(row, col, dir)) {
            replaceLineAttack(row, col, dir)

            val alreadyKing = board[row][col].isUpperCase()

            //king making check
            if (!alreadyKing) {
                if (isPlayerX && row + dir.rowOffset == 1 && xPoints < 12 || //attack moves 3 lines, so attacking row 1 will land at row 0
                        !isPlayerX && row + dir.rowOffset == 6 && oPoints < 12) {

                    replaceLine(row+dir.rowOffset, col+dir.colOffset, dir, getCurrentPlayer().toUpperCase())

                    if (isPlayerX) {
                        xPoints++
                        println("kinged X")
                    } else {
                        oPoints++
                        println("kinged Y")
                    }

                }

            } else {
                replaceLine(row, col, dir, board[row][col].toString())
            }


            if (isPlayerX) {
                oPoints--
            } else {
                xPoints--
            }

            println( "x points: $xPoints o points: $oPoints")
        }

        if (isGameOver()) {
            println("game over. won by ${getCurrentPlayer()}")
            gameWon = true
        }

    }


    private fun isInBounds(row: Int, col: Int, dir: Direction): Boolean {

        val replRow = row + dir.rowOffset
        val replCol = col + dir.colOffset
        return replRow >= 0 && replRow < board.size &&
                replCol >= 0 && replCol < board[row].length

    }

    private fun isBlankSpot(row: Int, col: Int):Boolean {

        return board[row][col] == '+'

    }

    private fun canMove(row: Int, col: Int, dir: Direction): Boolean {

        return isInBounds(row, col, dir) &&
                isBlankSpot(row + dir.rowOffset, col + dir.colOffset)

    }

    private fun canAttack(row: Int, col: Int, dir: Direction): Boolean {

        return canMove(row + dir.rowOffset, col + dir.colOffset, dir) &&
                isEnemyPiece(row + dir.rowOffset, col + dir.colOffset)

    }

    private fun isEnemyPiece(row: Int, col: Int): Boolean {

        val otherChar = if (getCurrentPlayer() == "x") 'o' else 'x'

        return board[row][col].toLowerCase() == otherChar

    }

    private fun replaceLine(row: Int, col: Int, dir: Direction, replaceChar: String, currentChar: String = "+") {

        board[row].replace(col, col+1, currentChar) //change current

        val replRow = row + dir.rowOffset
        val replCol = col + dir.colOffset


        board[replRow].replace(replCol, replCol+1, replaceChar)

    }

    private fun replaceLineAttack(row: Int, col: Int, dir: Direction) {

        val replaceChar = board[row][col].toString()

        val pieceAttacked = board[row + dir.rowOffset][col + dir.colOffset]
        println("attacked this $pieceAttacked")

        //replace current & next together
       replaceLine(row, col, dir, "+")

        //then repeat for line after, so 3 lines are changed.
        //with check for king (uppercase char)
        if (pieceAttacked.isUpperCase()) {
            replaceLine(row + dir.rowOffset, col + dir.colOffset, dir, replaceChar, pieceAttacked.toLowerCase().toString())
        } else {
            replaceLine(row + dir.rowOffset, col + dir.colOffset, dir, replaceChar)
        }


    }

    private fun isGameOver(): Boolean {
        return xPoints == 0 || oPoints == 0
    }

    fun getCurrentPlayer(): String = if (isPlayerX) "x" else "o"

    fun changePlayer() {
        isPlayerX = !isPlayerX
    }

    fun checkMax(startRow: Int, startCol: Int) {

        val playerChar = if (isPlayerX) 'x' else 'o'

        if (board[startRow][startCol].toLowerCase() == playerChar) {

            val enemyPiecesLeft = makeEnemyPiecesArray()

            board[startRow][startCol] = '+'

            val max = findMaxPossiblePoints(startRow, startCol, enemyPiecesLeft, mutableListOf())
            println("max possible from here is $max")

            board[startRow][startCol] = playerChar

        } else {

            println("player $playerChar has no piece at ${startRow+1} ${startCol+1}")

        }

    }

    private fun makeEnemyPiecesArray(): Array<IntArray> {

        var enemyPiecesArray = Array(8){ IntArray(8) }

        val enemyChar = if (isPlayerX) 'o' else 'x'

        board.indices.forEach { row ->
            board[row].indices.forEach { col ->
                if (board[row][col] == enemyChar) {
                    enemyPiecesArray[row][col] = 1
                } else if (board[row][col] == enemyChar.toUpperCase()) { //king
                    enemyPiecesArray[row][col] = 2
                }
            }
        }

        return  enemyPiecesArray

    }

    private fun findMaxPossiblePoints(row: Int, col: Int, enemyPiecesLeft: Array<IntArray>, takenPieceLocs: MutableList<Int?>): Int {

        var locsStr = "took: ["

        for (loc in takenPieceLocs) {
            if (loc != null) {
                locsStr += "(${loc / 8 + 1},${loc % 8 + 1}), " //convert to row & col (+1 for non-zero display index)
            }
        }

        locsStr += "]"

        var max = 0

        for (direction in values()) {

            val checkRow = row + direction.rowOffset
            val checkCol = col + direction.colOffset

            val checkLoc = (row + direction.rowOffset) * 8 + (col + direction.colOffset)

            var currentMax = 0

            if (
                    isInBounds(checkRow, checkCol, direction) &&
                    isEnemyPiece(checkRow, checkCol) &&
                    enemyPiecesLeft[checkRow][checkCol] > 0 &&
                    isBlankSpot(checkRow + direction.rowOffset, checkCol + direction.colOffset) //checking "jump" spot free
            ) {
                println("${row+1} ${col+1}: $direction ok. \t\t $locsStr -> (${checkRow+1}, ${checkCol+1})")
                takenPieceLocs.add(checkLoc)
                //mark piece as taken. decrementing as king counts as 2 pieces and can be captured twice.
                enemyPiecesLeft[checkRow][checkCol]--

                currentMax = 1 + findMaxPossiblePoints(checkRow + direction.rowOffset, checkCol + direction.colOffset, enemyPiecesLeft, takenPieceLocs)

            }

            if (currentMax > max) {
                max = currentMax
            }
        }


        return max

    }

}