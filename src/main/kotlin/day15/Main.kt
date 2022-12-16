package day15

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import util.Util
import util.Util.shouldBe
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.max

data class Coordinate(val x: Int, val y: Int)

suspend fun main() {
    val testSensorsAndBeacons = parseSensorMapFromFile("/day15/test_input.txt")
    star1(testSensorsAndBeacons, 10) shouldBe 26
    star2(testSensorsAndBeacons, 0..20, 0..20).tuningFrequency() shouldBe BigInteger.valueOf(56000011)

    println("star 1: ${star1(parseSensorMapFromFile("/day15/input.txt"), 2000000)}")
    println("star 2: ${star2(parseSensorMapFromFile("/day15/input.txt"), 0..4000000, 0..4000000).tuningFrequency()}")
}

fun Coordinate.tuningFrequency(): BigInteger {
    return this.x.toBigInteger() * BigInteger.valueOf(4000000) + y.toBigInteger()
}

// return number of coordinates where sensors CANNOT exist at y
fun star1(sensorsAndBeacons: Map<Coordinate, Coordinate>, targetY: Int): Int {
    val beacons = sensorsAndBeacons.values.toSet()
    val allCoordinates = sensorsAndBeacons.flatMap { listOf(it.key, it.value) }
    // totally arbitrary, but it works
    val minX = allCoordinates.minOf { it.x } - 10000000
    val maxX = allCoordinates.maxOf { it.x } + 10000000
    val rangesWhereBeaconsAreNotPossibleAtY: List<IntRange> = sensorsAndBeacons.map { (sensor, beacon) ->
        reverseManhattanDistance(sensor, manhattanDistance(sensor, beacon), targetY)
    }.fold(emptyList()) { acc, list ->
        acc.merge(list)
    }
    val xRange = minX .. maxX
    return xRange.count { x ->
        val cord = Coordinate(x, targetY)
        rangesWhereBeaconsAreNotPossibleAtY.any { it.contains(x) && cord !in beacons }
    }
}

val scope = CoroutineScope(Dispatchers.Default)

// find only possible place for beacon in xRange and yRange
suspend fun star2(sensorsAndBeacons: Map<Coordinate, Coordinate>, xRange: IntRange, yRange: IntRange): Coordinate {
    val jobs: List<Deferred<Coordinate?>> = yRange.map { y ->
        scope.async {
            val rangesWhereBeaconsAreNotPossibleAtY: List<IntRange> = sensorsAndBeacons.map { (sensor, beacon) ->
                reverseManhattanDistanceConstrained(
                    sensor,
                    manhattanDistance(sensor, beacon),
                    y,
                    xRange.first,
                    xRange.last
                )
            }.fold(emptyList()) { acc, list ->
                acc.merge(list)
            }
            if (rangesWhereBeaconsAreNotPossibleAtY.size > 1) {
                val xRangeWhereBeaconsArePossible =
                    rangesWhereBeaconsAreNotPossibleAtY.fold(xRange.toSet()) { acc, intRange ->
                        acc - intRange
                    }
                if (xRangeWhereBeaconsArePossible.isNotEmpty()) {
                    val x = xRangeWhereBeaconsArePossible.first()
                    return@async Coordinate(x, y)
                }
            }
            null
        }
    }
    jobs.forEach {
        val result = it.await()
        if (result != null) {
            scope.coroutineContext.cancelChildren()
            return result
        }
    }
    throw IllegalStateException("no result found")
}

fun List<IntRange>.merge(intRanges: List<IntRange>): List<IntRange> {
    return (this + intRanges).sortedBy { it.first }.fold(emptyList()) { acc: List<IntRange>, intRange: IntRange ->
        if (acc.isEmpty()) {
            listOf(intRange)
        } else {
            val last = acc.last()
            if (last.last + 1 >= intRange.first) {
                acc.dropLast(1) + listOf(last.first..max(intRange.last, last.last))
            } else {
                acc + listOf(intRange)
            }
        }
    }
}

fun List<IntRange>.merge(intRange: IntRange): List<IntRange> {
    return this.merge(listOf(intRange))
}

fun manhattanDistance(a: Coordinate, b: Coordinate): Int {
    return abs(a.x - b.x) + abs(a.y - b.y)
}

fun reverseManhattanDistance(sensor: Coordinate, manhattanDistance: Int, targetY: Int): IntRange {
    val startX = sensor.x - (manhattanDistance - abs(sensor.y - targetY))
    val endX = sensor.x + (manhattanDistance - abs(sensor.y - targetY))
    return startX..max(startX, endX)
}

fun reverseManhattanDistanceConstrained(sensor: Coordinate, manhattanDistance: Int, targetY: Int, minX: Int, maxX: Int): IntRange {
    val startX = sensor.x - (manhattanDistance - abs(targetY - sensor.y))
    val endX = sensor.x + (manhattanDistance - abs(targetY - sensor.y))
    return startX.coerceIn(minX..maxX)..max(startX, endX).coerceIn(minX..maxX)
}

fun parseSensorMapFromFile(filename: String): Map<Coordinate, Coordinate> {
    val lines = Util.resourcesFile(filename)?.readText()?.lines()?.filter { it.isNotEmpty() } ?: throw Exception("Could not read file")
    return lines.associate {
        val (sensor, beacon) = it.split(":")
        val (sensorX, sensorY) = sensor.split(",").map { it.trim().split("=")[1].toInt() }
        val (beaconX, beaconY) = beacon.split(",").map { it.trim().split("=")[1].toInt() }
        Coordinate(sensorX, sensorY) to Coordinate(beaconX, beaconY)
    }
}
