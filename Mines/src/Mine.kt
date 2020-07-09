package minesweeper

import java.lang.NumberFormatException
import java.util.*
import kotlin.random.Random

/**
 * A Minesweeper console game implementation in Kotlin.
 * Based on a project in JetBrains academy's Kotlin Developer course.
 */
class Mine(val mines: Int = 9, val height: Int = 9, val width: Int = 9) {

    //Mine locations generated later, to ensure the first move is not game over.
    private lateinit var mineLocs: MutableSet<Int>

    private val scanner = Scanner(System.`in`)

    private val numsList = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")

    private var board = Array(height) { Array<String>(width) { Mine.blankSymbol }} //Hidden board for actual mine & number placement etc
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
        const val mineSymbol = "X"
        const val markSymbol = "*" //User guess toggled mark
        const val openedSymbol = " "
    }

    private fun makeMineLocs(firstX: Int, firstY: Int) {
        var randLocs = mutableSetOf<Int>()

        while (randLocs.size < mines) {

            //Random location based on board size
            var nextLoc = Random.nextInt(0, height * width)

            //Convert to row, col to ensure initial click is not included
            //as mine location
            var nextX = nextLoc / 9
            var nextY = nextLoc % 9


            if (nextX == firstX && nextY == firstY ||
                    nextLoc == 0 && firstX == 0 && firstY == 0) { //<- hacky bugfix for location 0,0
                continue
            } else {
                randLocs.add(nextLoc)
            }


        }
        mineLocs = randLocs
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

        //Add mines to board
        var counter = 0

        for (i in board.indices) {
            for (j in board[i].indices) {
                if (mineLocs.contains(counter)) {
                    board[i][j] = Mine.mineSymbol
                }
                counter++
            }
        }

        //Add numbers to board, by checking surrounding mines count
        for (i in board.indices) {

            for (j in board[i].indices) {

                if (board[i][j] == Mine.mineSymbol) {
                    continue
                }

                val rowBefore = i > 0
                val rowAfter = i < board.size - 1

                val colBefore = j > 0
                val colAfter = j < board[i].size - 1

                var closeMines = 0

                //check previous row
                if (rowBefore) {
                    if (colBefore) {
                        if (board[i-1][j-1] == Mine.mineSymbol) {
                            closeMines++
                        }
                    }
                    if (board[i-1][j] == Mine.mineSymbol) {
                        closeMines++
                    }
                    if (colAfter) {
                        if (board[i-1][j+1] == Mine.mineSymbol) {
                            closeMines++
                        }
                    }
                }

                if (colBefore) {
                    if (board[i][j-1] == Mine.mineSymbol) {
                        closeMines++
                    }
                }
                if (colAfter) {
                    if (board[i][j+1] == Mine.mineSymbol) {
                        closeMines++
                    }
                }

                if (rowAfter) {
                    if (colBefore) {
                        if (board[i+1][j-1] == Mine.mineSymbol) {
                            closeMines++
                        }
                    }
                    if (board[i+1][j] == Mine.mineSymbol) {
                        closeMines++
                    }
                    if (colAfter) {
                        if (board[i+1][j+1] == Mine.mineSymbol) {
                            closeMines++
                        }
                    }
                }

                if(closeMines > 0) {
                    board[i][j] = closeMines.toString()
                }
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
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return
        }

        when(shownBoard[x][y]) {
            "1", "2", "3", "4", "5", "6", "7", "8" -> {
                return
            }
            Mine.openedSymbol -> return
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

            println("Move ${movesMade + 1}. Marked: $marked/$mines mines. Unrevealed: $unopenedTiles")
            print("Type row and column with 'mine' or 'free': ")

            var x = 0
            var y = 0

            try {

                val input = scanner.next()

                if (input == "quit" || input == "q" || input == "exit") {
                    gameOver = true
                    exited = true
                    return
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
                    println("mine / m: mark, free / f: check, exit / q: quit, help / h: help")
                    println("Choose a row and column and type them with a space.")
                    println("Then type the action you want.")
                    println("mine\t :-> mark or unmark a spot (*) as a mine ")
                    println("\t\t1 1 mine")
                    println("free\t :-> check a tile and reveal the area around it")
                    println("\t\t3 5 free")
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




}