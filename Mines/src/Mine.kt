package minesweeper

import java.lang.NumberFormatException
import java.util.*
import kotlin.random.Random

/**
 * A Minesweeper console game implementation in Kotlin.
 * Based on a project in JetBrains academy's Kotlin Developer course.
 */
class Mine(private val mines: Int = 9, private val height: Int = 9, private val width: Int = 9) {

    private var mineLocs = mutableSetOf<Int>()

    private val scanner = Scanner(System.`in`)

    private val numsList = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")

    private var board = Array(height) { Array<String>(width) { Mine.blankSymbol }} //Hidden board for making mine & numbers
    private var shownBoard = Array(height) { Array<String>(width) { Mine.blankSymbol}} //Board player can see

    private var marked = 0
    private var unopenedTiles = height * width //allow automatic game over if all except mines revealed

    private var gameOver = false
    private var won = false
    private var exited = false /* for correct game end message */

    private var movesMade = 0

    //Getters
    fun isGameOver(): Boolean = gameOver
    fun isExited(): Boolean = exited

    //Gameplay display helpers
    companion object Symbols {
        const val blankSymbol = "." //Unexplored tile
        const val mineSymbol = "\u00D7" //Unicode "multiply" symbol
        const val markSymbol = "*" //User guess toggled mark
        const val openedSymbol = " "
    }

    private fun makeMineLocs(firstX: Int, firstY: Int) {

        while (mineLocs.size < mines) {

            //Random location based on board size
            val nextLoc = Random.nextInt(0, height * width)

            //Convert to row, col to ensure initial click is not included
            //as mine location
            val nextX = nextLoc / width
            val nextY = nextLoc - nextX * width //allows different height & width


            if (nextX == firstX && nextY == firstY) {
                continue
            } else {
                mineLocs.add(nextLoc)
            }

        }

    }


    /** Counts surrounding mines & marks number on square of hidden board.
     *
     * params: firstX, firstY -> initial played guess location.
     * necessary to prevent game over condition on first move,
     * by delaying the generation of mine locations and avoiding
     * the first played location
     * */
    private fun addMinesToBoard(firstX: Int, firstY: Int) {

        makeMineLocs(firstX, firstY) //generate mine locations

        //Add mines & numbers to board, by checking surrounding mine locs count
        for (i in 0 until height * width) {

            val currentX = i / width
            val currentY = i - currentX * width

            if (i in mineLocs) {
                board[currentX][currentY] = Mine.mineSymbol
                continue
            }

            var surroundCount = 0

            val surroundLocs = mutableListOf<Int>()

            //top row
            if (currentX > 0) {

                if (currentY > 0) {
                    surroundLocs.add(i - width - 1)
                }

                surroundLocs.add(i - width)

                if (currentY < width - 1)  {
                    surroundLocs.add(i - width + 1)
                }

            }

            //middle l & r
            if (currentY > 0) {
                surroundLocs.add(i - 1)
            }

            if (currentY < width - 1) {
                surroundLocs.add(i + 1)
            }

            //bottom
            if (currentX < height - 1) {

                if (currentY > 0) {
                    surroundLocs.add(i + width - 1)
                }
                surroundLocs.add(i + width)

                if (currentY < width - 1) {
                    surroundLocs.add(i + width + 1)
                }
            }

            for (num in surroundLocs) {
                if (mineLocs.contains(num)) {
                    surroundCount++
                }
            }

            if (surroundCount > 0) {
                board[currentX][currentY] = surroundCount.toString()
            }

        }

    }


    fun print() {

        var topLine: String = "\t | "
        var nextLine: String = "\t-| "

        for (i in 1..width) {
            topLine += "$i "
            nextLine += "-" + " "
        }
        topLine += "|"
        nextLine += "|"

        println(topLine + "\n" + nextLine)
        var count = 1
        for (line in shownBoard) {
            print("\t$count| ")
            for (str in line) {
                print("$str ")
            }
            println("|")
            count++
        }

        println(nextLine)
    }



    /**
     * Function to place a mine mark on a given spot
     */
    private fun toggleMineGuessMark(x: Int, y: Int): Boolean {
        when (shownBoard[x][y]) {
            "1", "2", "3", "4", "5", "6", "7", "8" -> {
                println("-> There is a number here!")
                movesMade--
                return false
            }
            Mine.openedSymbol -> {
                println("-> This spot has already been checked")
                movesMade--
                return false
            }
            Mine.blankSymbol -> {
                shownBoard[x][y] = Mine.markSymbol
                marked++
            }
            Mine.markSymbol -> {
                shownBoard[x][y] = Mine.blankSymbol
                marked--
            }
        }

        return true
    }

    /**
     * Try to "clear" this spot and surrounding area,
     * revealing empty tiles up to the nearest number boundary zone
     * using a boundary fill algorithm.
     * Should only be triggered if a mine wasn't directly
     * stepped on
     */
    private fun explore(x: Int, y: Int) {

        //Bounds checking base case
        if (x < 0 || y < 0 || x >= height || y >= width) {
            return
        }

        when(shownBoard[x][y]) {
            "1", "2", "3", "4", "5", "6", "7", "8" -> {
                return
            }
            Mine.openedSymbol -> return
            Mine.markSymbol -> return
        }

        when (board[x][y]) {
            "1", "2", "3", "4", "5", "6", "7", "8" -> {
                shownBoard[x][y] = board[x][y]
                unopenedTiles--
                return
            }
            Mine.blankSymbol -> {
                shownBoard[x][y] = Mine.openedSymbol
                unopenedTiles--
            }
            Mine.mineSymbol -> return
        }


        //Checking boundary fill in only 4 directions - L, R, Top, Btm.
        explore(x-1, y)
        explore(x, y-1)
        explore(x, y+1)
        explore(x+1, y)

        //Diagonals - not used.
//        explore(x-1, y-1)
//        explore(x-1, y+1)
//        explore(x+1, y-1)
//        explore(x+1, y+1)

    }


    /**
     * Used in case of game over with a loss, to reveal all mine locations
     */
    fun displayFinalBoard() {
        for (i in shownBoard.indices) {
            for (j in shownBoard[i].indices) {

                if (board[i][j] == Mine.blankSymbol){
                    shownBoard[i][j] = Mine.openedSymbol
                } else {
                    shownBoard[i][j] = board[i][j]
                }

            }
        }
        print()
    }

    /**
     * In game menu
     */
    fun prompt() {

        //all revealed without stepping on a mine game over == must have won
        if (unopenedTiles == mines) {
            gameOver = true
            won = true

        } else {

            println("Move ${movesMade + 1}. Marked: $marked/$mines mines. Hidden: $unopenedTiles")
            print("Type row and column with 'mine' or 'free': ")

            val x: Int
            val y: Int

            try {

                val input = scanner.next().toLowerCase()

                if (input == "quit" || input == "q" || input == "exit") {
                    gameOver = true
                    exited = true
                    return
                }

                if (input == "help" || input == "h") {
                    showHelp()
                }

                x = input.toInt() - 1 //account for array 0 index
                y = scanner.next().toInt() - 1

                if (x > height - 1 || y > width - 1 || x < 0 || y < 0) {
                    println("-> That row and column isn't on the board.")
                    return
                }

            }
            catch (nfe: NumberFormatException) {
               println("-> Please enter two numbers with spaces after each.")
                return
            }

            val action = scanner.next().toLowerCase()

            movesMade++

            //Generate mines after first move
            if (movesMade == 1) {
                addMinesToBoard(x, y)
            }

            when (action) {
                "mine", "m" -> toggleMineGuessMark(x, y)
                "free", "f" -> {
                    if (shownBoard[x][y] == Mine.markSymbol) {

                        println("-> This spot is marked as a mine. Unmark to check around it.")
                        movesMade--

                    } else if (board[x][y] == Mine.mineSymbol) {

                        gameOver = true

                    } else {

                        if (shownBoard[x][y] == Mine.openedSymbol ||
                                numsList.contains(shownBoard[x][y])) {
                            println("-> ${x+1} ${y+1} was already checked.")
                            movesMade--
                        } else {
                            explore(x, y)
                        }

                    }
                }
                "exit", "q" -> {
                    gameOver = true
                    exited = true
                }
                "help", "h" -> {
                    movesMade--
                    showHelp()
                }
                else -> {
                    println("\"$action\" is not a recognized option.")
                    println("Type row and column numbers followed by mine / free / exit / help, like:")
                    println("\t\t1 1 free")
                    movesMade--
                    scanner.nextLine() //Clear scanner buffer
                }
            }
        }


        if (gameOver && won) {
            println("Congratulations! You found all the mines!")
        } else if (gameOver) {

            if(exited) {
                println("Exiting...")
            } else {
                println("You stepped on a mine. Game over.")
            }

        } else {
            print()
            prompt()
        }

    }

    private fun showHelp() {
        println()
        println("mine / m: mark, free / f: check, exit / q: quit, help / h: help")
        println()
        println("Choose a row and column and type them with a space.")
        println("Then type the action you want.")
        println("mine\t :-> mark or unmark a spot (*) as a mine ")
        println("\t\t1 1 mine")
        println("free\t :-> check a tile and reveal the area around it")
        println("\t\t3 5 free")
        println()
    }




}