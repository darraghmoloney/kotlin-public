package checker

import checker.Direction.*
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

        println("type your piece's row, column & move direction (tl, tr, bl, br)")
        print("[s to change player / q to quit]: ")

        val input = scanner.nextLine()

        val choices = input.split(" ")

        if (choices[0].isEmpty()) continue

        if (choices[0] == "quit" || choices[0] == "exit" || choices[0] == "q") {

            exit = true

        } else if (choices[0][0].toLowerCase() == 's') {

            c.changePlayer()

        } else {

            val nextRow = choices[0].toInt() - 1
            val nextCol = choices[1].toInt() - 1
            val direction = choices[2]


            var dir: Direction = TOP_LEFT

            when (direction) {
//                "tl" -> dir = TOP_LEFT
                "tr" -> dir = TOP_RIGHT
                "bl" -> dir = BOTTOM_LEFT
                "br" -> dir = BOTTOM_RIGHT
            }

            c.move(nextRow, nextCol, dir)
//            c.printBoard()
        }


    } while (!exit)
}