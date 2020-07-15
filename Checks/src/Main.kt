package checker

import checker.Direction.*
import java.lang.NumberFormatException
import java.util.Scanner

fun main() {

    val c = Checks()

    val scanner = Scanner(System.`in`)

    var exit = false

    do {

        if (c.gameWon) break

        c.printBoard()
        var currentPlayer = c.getCurrentPlayer()
        println("move for team $currentPlayer")

        println("type your piece's row, column & move direction (tl, tr, bl, br) or max")
        print("[s to change player /  q to quit]: ")

        val input = scanner.nextLine()

        val choices = input.split(" ")

        if (choices[0].isEmpty()) continue

        val choice1 = choices[0].trim()

        if (choice1 == "quit" || choice1 == "exit" || choice1 == "q") {

            exit = true

        } else if (choices[0][0] == 's') {

            c.changePlayer()

        } else {

            try {
                val nextRow = choices[0].trim().toInt() - 1
                val nextCol = choices[1].trim().toInt() - 1
                val action = choices[2]


                var dir: Direction? = null

                when (action) {
                    "tl" -> dir = TOP_LEFT
                    "tr" -> dir = TOP_RIGHT
                    "bl" -> dir = BOTTOM_LEFT
                    "br" -> dir = BOTTOM_RIGHT
                    "max" -> c.checkMax(nextRow, nextCol)
                    else -> println("not sure how to do that. try again...")
                }

                if (action != "max" && dir != null) {
                    c.move(nextRow, nextCol, dir)
                }

            }
            catch (nfe: NumberFormatException) {
                println("error: need 2 numbers and an action...")
            }
//            c.printBoard()
        }


    } while (!exit)
}