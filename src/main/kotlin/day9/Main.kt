package day9

import util.Util
import java.lang.RuntimeException
import kotlin.math.abs

fun main() {
    val testInput = Util.resourcesFile("/day9/test_input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedTestInput = testInput.split("\n")
        .filterNot { it.isEmpty() }
        .map { it.split(" ").let { Direction.fromCode(it.first().first()) to it.last().toInt() } }
    val testResult = uniqueTailVisitLocations(arrayifiedTestInput)
    require(testResult == 13) {
        "test failed: $testResult != 13"
    }
    val largerTestInput = Util.resourcesFile("/day9/larger_test_input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedLargerTestInput = largerTestInput.split("\n")
        .filterNot { it.isEmpty() }
        .map { it.split(" ").let { Direction.fromCode(it.first().first()) to it.last().toInt() } }
    val largerTestResult = uniqueTailVisitLocations(arrayifiedLargerTestInput, 10)
    require(largerTestResult == 36) {
        "test failed: $largerTestResult != 36"
    }

    val input = Util.resourcesFile("/day9/input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedInput = input.split("\n")
        .filterNot { it.isEmpty() }
        .map { it.split(" ").let { Direction.fromCode(it.first().first()) to it.last().toInt() } }
    println("star 1: ${uniqueTailVisitLocations(arrayifiedInput)}")
    println("star 2: ${uniqueTailVisitLocations(arrayifiedInput, 10)}")
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

// star 1
fun uniqueTailVisitLocations(headInstructions: List<Pair<Direction, Int>>): Int {
    var headCurrent = 0 to 0
    var tailCurrent = 0 to 0
    val visited = mutableSetOf(tailCurrent)
    headInstructions.forEach { direction ->
        repeat(direction.second) {
            headCurrent = when (direction.first) {
                Direction.UP -> headCurrent.first to headCurrent.second + 1
                Direction.DOWN -> headCurrent.first to headCurrent.second - 1
                Direction.LEFT -> headCurrent.first - 1 to headCurrent.second
                Direction.RIGHT -> headCurrent.first + 1 to headCurrent.second
            }
            if (!headCurrent.isAdjacent(tailCurrent)) {
                val firstDistance = headCurrent.first - tailCurrent.first
                val secondDistance = headCurrent.second - tailCurrent.second
                if (abs(firstDistance) == 2 && secondDistance == 0) {
                    tailCurrent = tailCurrent.copy(first = if (headCurrent.first > tailCurrent.first) tailCurrent.first + 1 else tailCurrent.first - 1)
                } else if (abs(secondDistance) == 2 && firstDistance == 0) {
                    tailCurrent = tailCurrent.copy(second = if (headCurrent.second > tailCurrent.second) tailCurrent.second + 1 else tailCurrent.second - 1)
                } else {
                    val movement = (if (abs(firstDistance) > 1) firstDistance / 2 else firstDistance) to (if (abs(secondDistance) > 1) secondDistance / 2 else secondDistance)
                    tailCurrent = tailCurrent.copy(
                        first = tailCurrent.first + movement.first,
                        second = tailCurrent.second + movement.second
                    )
                }
                visited.add(tailCurrent)
            }
        }
    }
    return visited.size
}

// checks if tail is adjacent to head including corners
fun Pair<Int, Int>.isAdjacent(tail: Pair<Int, Int>): Boolean {
    return tail.first in (this.first - 1)..(this.first + 1) && tail.second in (this.second - 1)..(this.second + 1)
}

// star 2
fun uniqueTailVisitLocations(headInstructions: List<Pair<Direction, Int>>, ropeSize: Int): Int {
    require(ropeSize > 2) { "rope size must be greater than 2" }
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
                    if (abs(firstDistance) == 2 && secondDistance == 0) {
                        ropeState[it.index] = ropeState[it.index].copy(first = if (ropeState[it.index - 1].first > ropeState[it.index].first) ropeState[it.index].first + 1 else ropeState[it.index].first - 1)
                    } else if (abs(secondDistance) == 2 && firstDistance == 0) {
                        ropeState[it.index] = ropeState[it.index].copy(second = if (ropeState[it.index - 1].second > ropeState[it.index].second) ropeState[it.index].second + 1 else ropeState[it.index].second - 1)
                    } else {
                        val movement = (if (abs(firstDistance) > 1) firstDistance / 2 else firstDistance) to (if (abs(secondDistance) > 1) secondDistance / 2 else secondDistance)
                        ropeState[it.index] = ropeState[it.index].copy(
                            first = ropeState[it.index].first + movement.first,
                            second = ropeState[it.index].second + movement.second
                        )
                    }
                }
            }
            visited.add(ropeState.last())
        }
    }
    return visited.size
}
