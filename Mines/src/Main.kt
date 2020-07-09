package minesweeper
import java.util.*


fun main() {

    var continuePlay = false
    val scanner = Scanner(System.`in`)

    do {

        print("How many mines do you want on the field? ")
        val mines = scanner.nextInt()
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

    } while (continuePlay)

    scanner.close()

}
