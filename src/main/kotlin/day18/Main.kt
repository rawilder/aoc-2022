package day18

import util.Util
import util.Util.shouldBe
import kotlin.system.measureTimeMillis

fun main() {
    val cubeCoordinates = parseFileToCoordinates("/day18/test_input.txt")
    Coordinate(0, 0, 0).faceNeighbors shouldBe setOf(
        Coordinate(-1, 0, 0),
        Coordinate(1, 0, 0),
        Coordinate(0, -1, 0),
        Coordinate(0, 1, 0),
        Coordinate(0, 0, -1),
        Coordinate(0, 0, 1)
    )
    measureTimeMillis {
        star1(cubeCoordinates) shouldBe 64
    }.also { println("test star 1 took $it ms") }

    measureTimeMillis {
        star2(cubeCoordinates) shouldBe 58
    }.also { println("test star 2 took $it ms") }

    measureTimeMillis {
        star2(cubeCoordinates, true) shouldBe 58
    }.also { println("recursive test star 2 took $it ms") }

    measureTimeMillis {
        println("star 1: ${star1(parseFileToCoordinates("/day18/input.txt"))}")
    }.also { println("star 1 took $it ms") }

    measureTimeMillis {
        println("star 2: ${star2(parseFileToCoordinates("/day18/input.txt"))}")
    }.also { println("star 2 took $it ms") }

    measureTimeMillis {
        println("recursive star 2: ${star2(parseFileToCoordinates("/day18/input.txt"), true)}")
    }.also { println("recursive star 2 took $it ms") }
}

fun star1(lavaCoords: Set<Coordinate>): Int {
    return lavaCoords.fold(0) { acc, cubeCoordinate ->
        acc + cubeCoordinate.faceNeighbors.count { it !in lavaCoords }
    }
}

enum class CubeState {
    LAVA, AIR, UNKNOWN
}

fun star2(lavaCoords: Set<Coordinate>, recursiveImpl: Boolean = false): Int {
    val maxX = lavaCoords.maxOf { it.x }
    val maxY = lavaCoords.maxOf { it.y }
    val maxZ = lavaCoords.maxOf { it.z }
    val cube: Array<Array<Array<CubeState>>> = Array(maxX + 1) { Array(maxY + 1) { Array(maxZ + 1) { CubeState.UNKNOWN } } }
    lavaCoords.forEach { cube[it.x][it.y][it.z] = CubeState.LAVA }
    // outer edge of cube
    var airCoords = mutableSetOf<Coordinate>()
    for (x in 0..maxX) {
        for (y in 0..maxY) {
            for (z in 0..maxZ) {
                if (x == 0 || y == 0 || z == 0 || x == maxX || y == maxY || z == maxZ) {
                    if (cube[x][y][z] == CubeState.UNKNOWN) {
                        cube[x][y][z] = CubeState.AIR
                        airCoords.add(Coordinate(x, y, z))
                    }
                }
            }
        }
    }
    if (recursiveImpl) {
        tryAtRecursive(cube, airCoords)
    } else markAllNeighborsOfAirAsAir(cube, airCoords)
    return lavaCoords.sumOf { c ->
        c.faceNeighbors.count { n ->
            !n.inCube(cube) || cube[n.x][n.y][n.z] == CubeState.AIR
        }
    }
}

fun markAllNeighborsOfAirAsAir(cube: Array<Array<Array<CubeState>>>, airCoords: Set<Coordinate>) {
    val queue = airCoords.toMutableList()
    while (queue.isNotEmpty()) {
        val coord = queue.removeFirst()
        coord.faceNeighbors.filter { n ->
            n.inCube(cube) && cube[n.x][n.y][n.z] == CubeState.UNKNOWN
        }.forEach { neighbor ->
            cube[neighbor.x][neighbor.y][neighbor.z] = CubeState.AIR
            queue.add(neighbor)
        }
    }
}

tailrec fun tryAtRecursive(cube: Array<Array<Array<CubeState>>>, cordsToCheck: Set<Coordinate>) {
    return if (cordsToCheck.isEmpty()) {
        return
    } else {
        val cord = cordsToCheck.first()
        val neighbors = cord.faceNeighbors.filter { n ->
            n.inCube(cube) && cube[n.x][n.y][n.z] == CubeState.UNKNOWN
        }
        if (cord.inCube(cube) && cube[cord.x][cord.y][cord.z] == CubeState.AIR) {
            neighbors.forEach { neighbor ->
                cube[neighbor.x][neighbor.y][neighbor.z] = CubeState.AIR
            }
        }
        tryAtRecursive(cube, (cordsToCheck - cord) + neighbors)
    }
}

data class Coordinate(val x: Int, val y: Int, val z: Int) {
    val faceNeighbors by lazy {
        setOf(
            Coordinate(x - 1, y, z),
            Coordinate(x + 1, y, z),
            Coordinate(x, y - 1, z),
            Coordinate(x, y + 1, z),
            Coordinate(x, y, z - 1),
            Coordinate(x, y, z + 1)
        )
    }

    fun inCube(cube: Array<Array<Array<CubeState>>>): Boolean {
        return x in cube.indices && y in cube[0].indices && z in cube[0][0].indices
    }
}

fun parseFileToCoordinates(resourcePath: String): Set<Coordinate> {
    return Util.resourcesFile(resourcePath)!!.readText().lines().filter { it.isNotEmpty() }.map {
        it.split(",").let { Coordinate(it[0].toInt(), it[1].toInt(), it[2].toInt()) }
    }.toSet()
}

