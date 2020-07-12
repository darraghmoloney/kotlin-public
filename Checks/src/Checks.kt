package checker

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

        println("trying to move from ${row+1} ${col+1} in $dir")

        val playerChar = if (isPlayerX) "x" else "o"

        if (board[row][col].toString() != playerChar) {
            println("No $playerChar at ${row+1} ${col+1}.")
            return
        }

        println("$playerChar currently at ${row+1} ${col+1}")

        if (canMove(row, col, dir)) {
            replaceLine(row, col, dir, playerChar)
            println("moving in $dir direction")
        } else if (canAttack(row, col, dir)) {
            attack(row, col, dir)
            println("attacking in $dir direction")
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

        return replRow >= 0 && replRow < board.size &&
                replCol >= 0 && replCol < board[row].length

    }

    private fun isBlankSpot(row: Int, col: Int):Boolean {

        if (board[row][col] !== '+') println("$row $col not blank - ${board[row][col]}")

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

        val otherChar = if (getCurrentPlayer()[0] == 'x') 'o' else 'x'

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

}