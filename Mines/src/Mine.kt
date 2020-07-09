package minesweeper

import java.util.*
import kotlin.random.Random

/**
 * A Minesweeper console game implementation in Kotlin.
 * Based on a project in JetBrains academy's Kotlin Developer course.
 */
class Mine(val mines: Int = 9, val height: Int = 9, val width: Int = 9) {

    //Mine locations generated later, to ensure the first move is not game over.
    private lateinit var mineLocs: MutableSet<Int>

    private var board = Array(height) { Array<String>(width) { Mine.blankSymbol }} //Hidden board for actual mine & number placement etc
    private var shownBoard = Array(height) { Array<String>(width) { Mine.blankSymbol}} //Board player can see

    private var marked = 0
    private var unopenedTiles = height * width //allow automatic game over if all except mines revealed

    private var gameOver = false
    private var won = false
    private var exited = false /* for correct game end message */

    private var movesMade = 0


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

            if (nextX == firstX && nextY == firstY || nextLoc == 0 && firstX == 0 && firstY == 0) {
                continue
            } else {
                randLocs.add(Random.nextInt(0, height * width))
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
    private fun markMines(firstX: Int, firstY: Int) {

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
        var newBoard = board
        for (i in newBoard.indices) {
            for (j in newBoard[i].indices) {

                if (newBoard[i][j] == Mine.mineSymbol) {
                    continue
                }

                val rowBefore = i > 0
                val rowAfter = i < newBoard.size - 1

                val colBefore = j > 0
                val colAfter = j < newBoard[i].size - 1

                var closeMines = 0

                //check previous row
                if (rowBefore) {
                    if (colBefore) {
                        if (newBoard[i-1][j-1] == Mine.mineSymbol) {
                            closeMines++
                        }
                    }
                    if (newBoard[i-1][j] == Mine.mineSymbol) {
                        closeMines++
                    }
                    if (colAfter) {
                        if (newBoard[i-1][j+1] == Mine.mineSymbol) {
                            closeMines++
                        }
                    }
                }

                if (colBefore) {
                    if (newBoard[i][j-1] == Mine.mineSymbol) {
                        closeMines++
                    }
                }
                if (colAfter) {
                    if (newBoard[i][j+1] == Mine.mineSymbol) {
                        closeMines++
                    }
                }

                if (rowAfter) {
                    if (colBefore) {
                        if (newBoard[i+1][j-1] == Mine.mineSymbol) {
                            closeMines++
                        }
                    }
                    if (newBoard[i+1][j] == Mine.mineSymbol) {
                        closeMines++
                    }
                    if (colAfter) {
                        if (newBoard[i+1][j+1] == Mine.mineSymbol) {
                            closeMines++
                        }
                    }
                }

                if(closeMines > 0) {
                    newBoard[i][j] = closeMines.toString()
                }
            }
        }

        board = newBoard
    }


    fun print() {

        var topLine: String = " | "
        var nextLine: String = "-| "

        for (i in 1..width) {
            topLine += "$i "
            nextLine += "-" + " "
        }
        topLine += "|"
        nextLine += "|"

        println(topLine + "\n" + nextLine)
        var count = 1
        for (line in shownBoard) {
            print("$count| ")
            for (str in line) {
                print(str + " ")
            }
            println("|")
            count++
        }

        println(nextLine)
    }



    /**
     * Function to place a mine mark on a given spot
     */
    private fun checkSpot(x: Int, y: Int): Boolean {
        when (shownBoard[x][y]) {
            "1", "2", "3", "4", "5", "6", "7", "8" -> {
                println("There is a number here!")
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

//        val numbers = arrayOf<String>("1", "2", "3", "4", "5", "6", "7", "8")

        //Bounds checking
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return
        }

        when(shownBoard[x][y]) {
            "1", "2", "3", "4", "5", "6", "7", "8" -> {
//                if ( x > 1) {
//                    if (shownBoard[x-1][y] == Mine.blankSymbol && board[x-1][y] != Mine.mineSymbol) {
//                        shownBoard[x-1][y] = board[x-1][y]
//                    }
//                }
//                if ( x + 1 < width - 1) {
//                    if (shownBoard[x+1][y] == Mine.blankSymbol && board[x+1][y] != Mine.mineSymbol) {
//                        shownBoard[x+1][y] = board[x+1][y]
//                    }
//                }
//
//                if ( y > 1) {
//                    if (shownBoard[x][y-1] == Mine.blankSymbol && board[x][y-1] != Mine.mineSymbol) {
//                        shownBoard[x][y-1] = board[x-1][y]
//                    }
//                }
//                if ( y + 1 < height - 1) {
//                    if (shownBoard[x][y+1] == Mine.blankSymbol && board[x][y+1] != Mine.mineSymbol) {
//                        shownBoard[x][y+1] = board[x][y+1]
//                    }
//                }

                return
            }
            Mine.openedSymbol -> return
//
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
            // else -> shownBoard[x][y] = board[x][y]
        }


        //Checking boundary fill in 4 directions.
//        explore(x-1, y-1)
        explore(x-1, y)
//        explore(x-1, y+1)

        explore(x, y-1)
        explore(x, y+1)

//        explore(x+1, y-1)
        explore(x+1, y)
//        explore(x+1, y+1)

    }



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

        val scanner = Scanner(System.`in`)


        //all revealed without stepping on a mine game over == must have won
        if (unopenedTiles == mines) {
            gameOver = true
            won = true

        } else {

            println("Move ${movesMade + 1}. Marked: $marked/$mines mines. Unrevealed: $unopenedTiles")
            print("Type row and column with 'mine' or 'free': ")

            var x = 0
            var y = 0

            x = scanner.next().toInt() - 1 //account for array 0 index
            y = scanner.next().toInt() - 1

            val action = scanner.next()

            movesMade++

            //Generate mines after first move
            if (movesMade == 1) {
                markMines(x, y)
            }

            when (action) {
                "mine", "m" -> checkSpot(x, y)
                "free", "f" -> {
                    if (shownBoard[x][y] == Mine.markSymbol) {

                        println("This spot is marked as a mine. Unmark to check around it.")
                        movesMade--

                    } else if (board[x][y] == Mine.mineSymbol) {

                        gameOver = true

                    } else {

                        explore(x, y)

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
            }
        }


        if (gameOver && won) {
            println("Congratulations! You found all the mines!")
        } else if (gameOver) {
            if(exited) {
                println("Exiting...")
            } else {
                println("Game over. You stepped on a mine.")
            }
        } else {
            print()
            prompt()
        }

    }

    fun isGameOver(): Boolean = gameOver
    fun isExited(): Boolean = exited


}