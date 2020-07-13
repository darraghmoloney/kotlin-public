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

    private var xPoints: Int = 0
    private var oPoints: Int = 0

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
//        println("  _________________")

        for (lineNum in board.indices)  {
            print("${lineNum+1} |")
            for (c in board[lineNum]) { //adding padding by printing char by char
                print("$c ")
            }
            println()
        }
    }

    /* TODO: implement King-making */
    fun move(row: Int, col: Int, dir: Direction) {

        println("trying to move from ${row+1} ${col+1} to the $dir")

        val playerChar = getCurrentPlayer()

        if (board[row][col].toString() != playerChar) {
            println("No $playerChar at ${row+1} ${col+1}.")
            return
        }

        println("$playerChar currently at ${row+1} ${col+1}")

        if (canMove(row, col, dir)) {
            replaceLine(row, col, dir, playerChar)
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
            if (isPlayerX) {
                xPoints++
            } else {
                oPoints++
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

//        if (replCol < 0) println("replCol (offset) $replCol col too low for ${row+1} ${col+1} $dir")
//        if (replCol > board[row].length) println("row too high for ${row+1} ${col+1} $dir")

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

        return board[row][col] == otherChar

    }

    private fun replaceLine(row: Int, col: Int, dir: Direction, replaceChar: String) {

        board[row].replace(col, col+1, "+") //change current

        val replRow = row + dir.rowOffset
        val replCol = col + dir.colOffset

        board[replRow].replace(replCol, replCol+1, replaceChar)

    }

    private fun replaceLineAttack(row: Int, col: Int, dir: Direction) {

        val replaceChar = getCurrentPlayer()
        replaceLine(row, col, dir, "+")
        replaceLine(row + dir.rowOffset, col + dir.colOffset, dir, replaceChar)

    }

    private fun isGameOver(): Boolean {
        return xPoints == 12 || oPoints == 12
    }

    fun getCurrentPlayer(): String = if (isPlayerX) "x" else "o"

    fun changePlayer() {
        isPlayerX = !isPlayerX
    }

    fun checkMax(startRow: Int, startCol: Int) {

        val playerChar = if (isPlayerX) 'x' else 'o'

        if (board[startRow][startCol] == playerChar) {

            board[startRow][startCol] = '+'

            val max = findMaxPossiblePoints(startRow, startCol, mutableSetOf())
            println("max possible from here is $max")

            board[startRow][startCol] = playerChar

        } else {

            println("player $playerChar has no piece at ${startRow+1} ${startCol+1}")

        }

    }

    private fun findMaxPossiblePoints(row: Int, col: Int, takenPieceLocs: MutableSet<Int?>): Int {

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

            val checkLoc = (row + direction.rowOffset) * 8 + (col + direction.colOffset) //Int representation of row & col

            var currentMax = 0

            if (
                    isInBounds(checkRow, checkCol, direction) &&
                    isEnemyPiece(checkRow, checkCol) &&
                    !takenPieceLocs.contains(checkLoc) && //don't take same piece twice
                    isBlankSpot(checkRow + direction.rowOffset, checkCol + direction.colOffset) //checking "jump" spot free
            ) {
                println("${row+1} ${col+1}: $direction ok. \t\t $locsStr -> (${checkRow+1}, ${checkCol+1})")
                takenPieceLocs.add(checkLoc)

                currentMax = 1 + findMaxPossiblePoints(checkRow + direction.rowOffset, checkCol + direction.colOffset, takenPieceLocs)

            }

            if (currentMax > max) {
                max = currentMax
            }
        }


        return max

    }

}