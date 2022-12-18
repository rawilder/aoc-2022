package day17

import util.Util
import util.Util.shouldBe
import kotlin.system.measureTimeMillis

val initialMap: (Long, Long, Long) -> RockMap = { leftWall, rightWall, floor ->
    ((leftWall + 1) until rightWall).fold(emptyMap()) { map, x ->
        map + (x to (map[x] ?: emptySet()) + floor)
    }
}

typealias RockMap = Map<Long, Set<Long>>

fun main() {
    val directions = parseGasVentDirectionFromFile("/day17/test_input.txt")
    measureTimeMillis {
        star1(2022, directions, 0, 8, 0) shouldBe 3068
    }.also { println("test star 1: $it ms") }
    measureTimeMillis {
        star1(1000000000000, directions, 0, 8, 0) shouldBe 1514285714288L
    }

    println("star 1: ${star1(2022, parseGasVentDirectionFromFile("/day17/input.txt"), 0, 8, 0)}")
}

fun star1(numRocks: Long, gasVentDirections: Iterator<Direction>, leftWall: Long, rightWall: Long, floor: Long): Long {
    var currentNumRocks = 0
    val finalMap = rockFuns.takeWhile { currentNumRocks++ < numRocks }.fold(initialMap(leftWall, rightWall, floor)) { rockMap, rockFun ->
        val rock = rockFun(leftWall, rockMap.values.maxOf { it.max() })
        val droppedRock = dropRock(rock, rockMap, gasVentDirections)
        droppedRock.fold(rockMap) { acc, coordinate ->
            acc + (coordinate.x to (acc[coordinate.x] ?: emptySet()) + coordinate.y)
        }
    }
    return finalMap.values.maxOf { it.max() }
}

// do what star 1 does but less memory and faster
fun star2(): Long {
   return 0
}

tailrec fun dropRock(rock: Set<Coordinate>, rockMap: RockMap, directions: Iterator<Direction>): Set<Coordinate> {
    val afterGasVent = directions.next().moveRock(rock, rockMap)
    val movedDown = Direction.DOWN.moveRock(afterGasVent, rockMap)
    return if (movedDown == afterGasVent) {
        afterGasVent
    } else dropRock(movedDown, rockMap, directions)
}


fun parseGasVentDirectionFromFile(filename: String): Iterator<Direction> {
    val directions = Util.resourcesFile(filename)!!.readText().lines().first().map {
        Direction.fromSymbol(it)
    }
    return generateSequence(0) {
        if (it == directions.size - 1) 0 else it + 1
    }.map { directions[it] }.iterator()
}

data class Coordinate(val x: Long, val y: Long)

enum class Direction(val symbol: Char) {
    UP('^'), DOWN('v'), LEFT('<'), RIGHT('>');

    fun moveRock(rock: Set<Coordinate>, rockMap: RockMap): Set<Coordinate> {
        // move whole rock if not blocked
        return rock.fold(emptySet()) { acc, coordinate ->
            val moved = this.move(coordinate, rockMap)
            if (moved == coordinate) {
                return@moveRock rock
            }
            acc + moved
        }
    }

    private fun move(coordinate: Coordinate, rockMap: RockMap): Coordinate {
        val openAir = rockMap.keys.let {
            it.min()..it.max()
        }
        return when (this) {
            LEFT -> {
                if (coordinate.x - 1 in openAir && rockMap[coordinate.x - 1]?.contains(coordinate.y) == false) {
                    Coordinate(coordinate.x - 1, coordinate.y)
                } else {
                    coordinate
                }
            }
            RIGHT -> {
                if (coordinate.x + 1 in openAir && rockMap[coordinate.x + 1]?.contains(coordinate.y) == false) {
                    Coordinate(coordinate.x + 1, coordinate.y)
                } else {
                    coordinate
                }
            }
            UP -> {
                if (rockMap[coordinate.x]?.contains(coordinate.y + 1) == false) {
                    Coordinate(coordinate.x, coordinate.y + 1)
                } else {
                    coordinate
                }
            }
            DOWN -> {
                if (rockMap[coordinate.x]?.contains(coordinate.y - 1) == false) {
                    Coordinate(coordinate.x, coordinate.y - 1)
                } else {
                    coordinate
                }
            }
        }
    }

    companion object {
        fun fromSymbol(symbol: Char): Direction {
            return values().first { it.symbol == symbol }
        }
    }
}

val rockFuns = generateSequence(0) {
    if (it == 4) 0 else it + 1
}.map {
    rocksMap[it]!!
}

//Each rock appears so that its left edge is two units away from the left wall
// and its bottom edge is three units above the highest rock in the room

////
////##
////##
val rocksMap: Map<Int, (Long, Long) -> Set<Coordinate>> = mapOf(
    0 to { leftWall, highestRock ->
        setOf(
            Coordinate(leftWall + 3, highestRock + 4),
            Coordinate(leftWall + 4, highestRock + 4),
            Coordinate(leftWall + 5, highestRock + 4),
            Coordinate(leftWall + 6, highestRock + 4)
        )
         },
    1 to { leftWall, highestRock ->
        setOf(
            Coordinate(leftWall + 3, highestRock + 5),
            Coordinate(leftWall + 4, highestRock + 5),
            Coordinate(leftWall + 5, highestRock + 5),
            Coordinate(leftWall + 4, highestRock + 4),
            Coordinate(leftWall + 4, highestRock + 6),
        )
         },
    2 to { leftWall, highestRock ->
        setOf(
            Coordinate(leftWall + 3, highestRock + 4),
            Coordinate(leftWall + 4, highestRock + 4),
            Coordinate(leftWall + 5, highestRock + 4),
            Coordinate(leftWall + 5, highestRock + 5),
            Coordinate(leftWall + 5, highestRock + 6),
        )
         },
    3 to { leftWall, highestRock ->
        setOf(
            Coordinate(leftWall + 3, highestRock + 4),
            Coordinate(leftWall + 3, highestRock + 5),
            Coordinate(leftWall + 3, highestRock + 6),
            Coordinate(leftWall + 3, highestRock + 7),
        )
         },
    4 to { leftWall, highestRock ->
        setOf(
            Coordinate(leftWall + 3, highestRock + 4),
            Coordinate(leftWall + 4, highestRock + 4),
            Coordinate(leftWall + 3, highestRock + 5),
            Coordinate(leftWall + 4, highestRock + 5)
        )
         }
)
