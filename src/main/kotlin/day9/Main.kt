package day9

import util.Util
import java.lang.RuntimeException
import kotlin.math.abs

fun main() {
    val testResult = uniqueTailVisitLocations(parseFileToInstructions("/day9/test_input.txt"), 2)
    require(testResult == 13) {
        "test failed: $testResult != 13"
    }

    val largerTestResult = uniqueTailVisitLocations(parseFileToInstructions("/day9/larger_test_input.txt"), 10)
    require(largerTestResult == 36) {
        "test failed: $largerTestResult != 36"
    }

    val instructions = parseFileToInstructions("/day9/input.txt")
    println("star 1: ${uniqueTailVisitLocations(instructions, 2)}")
    println("star 2: ${uniqueTailVisitLocations(instructions, 10)}")
}

fun parseFileToInstructions(file: String): List<Pair<Direction, Int>> {
    return Util.resourcesFile(file)?.readText()?.split("\n")
        ?.filterNot { it.isEmpty() }
        ?.map { it.split(" ").let { Direction.fromCode(it.first().first()) to it.last().toInt() } }
        ?: throw RuntimeException("input not found")
}

enum class Direction(val code: Char) {
    UP('U'),
    DOWN('D'),
    LEFT('L'),
    RIGHT('R');

    companion object {
        fun fromCode(code: Char): Direction {
            return values().first { it.code == code }
        }
    }
}

// checks if tail is adjacent to head including corners
fun Pair<Int, Int>.isAdjacent(tail: Pair<Int, Int>): Boolean {
    return tail.first in (this.first - 1)..(this.first + 1) && tail.second in (this.second - 1)..(this.second + 1)
}

// star 1 & 2 refactored to be together
fun uniqueTailVisitLocations(headInstructions: List<Pair<Direction, Int>>, ropeSize: Int): Int {
    require(ropeSize >= 2) { "rope size must be greater than or equal to 2" }
    val ropeState = Array(ropeSize) { 0 to 0}

    val visited = mutableSetOf(ropeState.last())
    headInstructions.forEach { direction ->
        repeat(direction.second) {
            ropeState[0] = when (direction.first) {
                Direction.UP -> ropeState[0].first to ropeState[0].second + 1
                Direction.DOWN -> ropeState[0].first to ropeState[0].second - 1
                Direction.LEFT -> ropeState[0].first - 1 to ropeState[0].second
                Direction.RIGHT -> ropeState[0].first + 1 to ropeState[0].second
            }
            ropeState.withIndex().drop(1).forEach {
                if (!ropeState[it.index - 1].isAdjacent(it.value)) {
                    val firstDistance = ropeState[it.index - 1].first - it.value.first
                    val secondDistance = ropeState[it.index - 1].second - it.value.second
                    val movement = (if (abs(firstDistance) > 1) firstDistance / 2 else firstDistance) to (if (abs(secondDistance) > 1) secondDistance / 2 else secondDistance)
                    ropeState[it.index] = ropeState[it.index].copy(
                        first = ropeState[it.index].first + movement.first,
                        second = ropeState[it.index].second + movement.second
                    )
                }
            }
            visited.add(ropeState.last())
        }
    }
    return visited.size
}
