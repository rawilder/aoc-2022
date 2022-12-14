package day14

import util.Util
import util.Util.shouldBe

typealias Coordinate = Pair<Int, Int>

fun main() {
    val rocks = parseRockMapFromFile("/day14/test_input.txt")
    val testMapState = rocks.associateWith {
        MAP_STATE.ROCK
    }
    howMuchSandAtRestWhenFallingToAbyss(testMapState, Coordinate(500, 0)) shouldBe 24
    val mapState = parseRockMapFromFile("/day14/input.txt")
        .associateWith {
            MAP_STATE.ROCK
        }
    println("star 1: ${howMuchSandAtRestWhenFallingToAbyss(mapState, Coordinate(500, 0))}")
    println("star 2: ${howMuchSandAtRestWhenSourceIsAtRest(mapState, Coordinate(500, 0))}")
}

fun howMuchSandAtRestWhenSourceIsAtRest(mapState: Map<Coordinate, MAP_STATE>, sandSourceCoordinate: Coordinate): Int {
    val mutableMapState = mapState.toMutableMap().withDefault { MAP_STATE.EMPTY }
    val floorY = mutableMapState.keys.maxOf { it.second } + 2
    val sand = mutableListOf<Coordinate>()
    var isSourceAtRest = false
    while(!isSourceAtRest) {
        sand.add(sandSourceCoordinate)
        val toRemove = mutableListOf<Int>()
        sand.toList().withIndex().forEach { (index, coordinate) ->
            val (x, y) = coordinate
            val down = Pair(x, y + 1)
            val left = Pair(x - 1, y + 1)
            val right = Pair(x + 1, y + 1)
            if (mutableMapState.getValueOrScanDefault(down, floorY) == MAP_STATE.EMPTY) {
                sand[index] = down
                mutableMapState[down] = MAP_STATE.SAND_MOVING
                mutableMapState[coordinate] = MAP_STATE.EMPTY
            } else if (mutableMapState.getValueOrScanDefault(left, floorY) == MAP_STATE.EMPTY) {
                sand[index] = left
                mutableMapState[left] = MAP_STATE.SAND_MOVING
                mutableMapState[coordinate] = MAP_STATE.EMPTY
            } else if (mutableMapState.getValueOrScanDefault(right, floorY) == MAP_STATE.EMPTY) {
                sand[index] = right
                mutableMapState[right] = MAP_STATE.SAND_MOVING
                mutableMapState[coordinate] = MAP_STATE.EMPTY
            } else {
                toRemove.add(index)
                mutableMapState[coordinate] = MAP_STATE.SAND_AT_REST
            }
        }
        var indexesRemoved = 0
        toRemove.forEach {
            sand.removeAt(it - indexesRemoved)
            indexesRemoved++
        }
        if (sand.isEmpty()) {
            isSourceAtRest = true
        }
    }
    return mutableMapState.values.count { it == MAP_STATE.SAND_AT_REST }
}

fun MutableMap<Coordinate, MAP_STATE>.getValueOrScanDefault(coordinate: Coordinate, floorY: Int): MAP_STATE {
    return getOrPut(coordinate) {
        if (coordinate.second >= floorY) {
            MAP_STATE.ROCK
        } else {
            MAP_STATE.EMPTY
        }
    }
}

fun howMuchSandAtRestWhenFallingToAbyss(mapState: Map<Coordinate, MAP_STATE>, sandSourceCoordinate: Coordinate): Int {
    val mutableMapState = mapState.toMutableMap().withDefault { MAP_STATE.EMPTY }
    val sand = mutableListOf<Coordinate>()
    var isSandFallingIntoAbyss = false
    while(!isSandFallingIntoAbyss) {
        sand.add(sandSourceCoordinate)
        val toRemove = mutableListOf<Int>()
        sand.toList().withIndex().forEach { (index, coordinate) ->
            val (x, y) = coordinate
            val down = Pair(x, y + 1)
            val left = Pair(x - 1, y + 1)
            val right = Pair(x + 1, y + 1)
            if (mutableMapState.getValue(down) == MAP_STATE.EMPTY) {
                sand[index] = down
                mutableMapState[down] = MAP_STATE.SAND_MOVING
                mutableMapState[coordinate] = MAP_STATE.EMPTY
            } else if (mutableMapState.getValue(left) == MAP_STATE.EMPTY) {
                sand[index] = left
                mutableMapState[left] = MAP_STATE.SAND_MOVING
                mutableMapState[coordinate] = MAP_STATE.EMPTY
            } else if (mutableMapState.getValue(right) == MAP_STATE.EMPTY) {
                sand[index] = right
                mutableMapState[right] = MAP_STATE.SAND_MOVING
                mutableMapState[coordinate] = MAP_STATE.EMPTY
            } else {
                toRemove.add(index)
                mutableMapState[coordinate] = MAP_STATE.SAND_AT_REST
            }
        }
        var indexesRemoved = 0
        toRemove.forEach {
            sand.removeAt(it - indexesRemoved)
            indexesRemoved++
        }
        val furthestSand = sand.maxBy { it.second }!!
        if (furthestSand.second > mapState.keys.maxBy { it.second }!!.second) {
            isSandFallingIntoAbyss = true
        }
    }
    return mutableMapState.values.count { it == MAP_STATE.SAND_AT_REST }
}

enum class MAP_STATE {
    SAND_AT_REST,
    SAND_MOVING,
    EMPTY,
    ROCK
}

// 498,4 -> 498,6 -> 496,6
// 503,4 -> 502,4 -> 502,9 -> 494,9
fun parseRockMapFromFile(filename: String): Set<Coordinate> {
    val lines = Util.resourcesFile(filename)?.readText()?.lines()?.filter { it.isNotEmpty() } ?: throw Exception("Could not read file")
    return lines.map { it.split(" -> ") }
        .map { it.map { it.split(",").map { it.toInt() }.let { it[0] to it[1] } } }
        .fold<List<Coordinate>, Set<Coordinate>>(emptySet()) { acc, rockPathCords ->
            acc + rockPathCords.zipWithNext().flatMap { (cord1, cord2) ->
                val (x1, y1) = cord1
                val (x2, y2) = cord2
                when {
                    x1 == x2 -> (y1 progressionTo y2).map { x1 to it }
                    y1 == y2 -> (x1 progressionTo x2).map { it to y1 }
                    else -> throw Exception("Invalid rock path")
                }
            }
        }.toSet()
}

infix fun Int.progressionTo(other: Int): IntProgression {
    return when {
        this < other -> this..other
        this > other -> this downTo other
        else -> IntRange.EMPTY
    }
}
