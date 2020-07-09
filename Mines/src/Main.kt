package minesweeper
import java.lang.NumberFormatException
import java.util.*


fun main() {

    var continuePlay = false
    val scanner = Scanner(System.`in`)

    do {

        print("How many mines do you want on the field? ")


        try {

            val input = scanner.nextLine().substringBefore(" ")

            if (input == "quit" || input == "q" || input == "exit") {
                break
            }

            var mines = input.toInt()


            if (mines < 0 || mines >= 81) {
                mines = 6
                println("Mines set to 6.")
            }


            var game = Mine(mines)

            game.print()


            while (!game.isGameOver()) {
                game.prompt()
            }

            if (!game.isExited()) {  //display final board on game win/loss only, not simple exit
                game.displayFinalBoard()
            }

            print("Play again? (y): ")
            val choice = scanner.next().toLowerCase()
            continuePlay = (choice == "y" || choice == "yes")

            if (!continuePlay) println("Goodbye.")

            scanner.nextLine()

        }
        catch (nfe: NumberFormatException) {
            println("Mines must be a number. If you want to quit, type exit.")
            continuePlay = true
        }

    } while (continuePlay)

    scanner.close()

}
