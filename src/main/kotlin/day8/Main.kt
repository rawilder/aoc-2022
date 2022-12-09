package day8

import util.Util
import java.lang.RuntimeException

fun main() {
    val testInput = Util.resourcesFile("/day8/test_input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedTestInput = testInput.split("\n").filterNot { it.isEmpty() }.map { it.split("").filter { it.isNotEmpty() }.map { it.toInt() }.toTypedArray() }.toTypedArray()
    val testResult = treesVisible(arrayifiedTestInput)
    require(testResult == 21) {
        "test failed: $testResult != 21"
    }
    var testScenicResult = highestScenicScore(arrayifiedTestInput)
    require(testScenicResult == 8) {
        "test failed: $testScenicResult != 8"
    }

    val input = Util.resourcesFile("/day8/input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedInput = input.split("\n").filterNot { it.isEmpty() }.map { it.split("").filter { it.isNotEmpty() }.map { it.toInt() }.toTypedArray() }.toTypedArray()
    val result = treesVisible(arrayifiedInput)
    println("star 1: $result")
    val scenicResult = highestScenicScore(arrayifiedInput)
    println("star 2: $scenicResult")
}

// star 1
fun treesVisible(treeGrid: Array<Array<Int>>): Int {
    val minY = 1
    val maxY = treeGrid.size - 1
    val minX = 1
    val maxX = treeGrid[0].size - 1
    var treesVisible = 0
    // trees are visible if all trees between it and the edge is shorter than it
    for (y in minY until maxY) {
        for (x in minX until maxX) {
            val currTree = treeGrid[y][x]
            val yUpRange = (y - 1 downTo 0)
            val yDownRange = (y + 1 until treeGrid.size)
            val xLeftRange = (x - 1 downTo 0)
            val xRightRange = (x + 1 until treeGrid[0].size)
            val tallerUpTree = yUpRange.firstOrNull { treeGrid[it][x] >= currTree }?.let { treeGrid[it][x] }
            val tallerDownTree = yDownRange.firstOrNull { treeGrid[it][x] >= currTree }?.let { treeGrid[it][x] }
            val tallerLeftTree = xLeftRange.firstOrNull { treeGrid[y][it] >= currTree }?.let { treeGrid[y][it] }
            val tallerRightTree = xRightRange.firstOrNull { treeGrid[y][it] >= currTree }?.let { treeGrid[y][it] }
            if (tallerUpTree == null || tallerDownTree == null || tallerLeftTree == null || tallerRightTree == null) {
                treesVisible++
            }
        }
    }
    // add edges to trees visible
    return treesVisible + treeGrid.size * 4 - 4
}

// star 2
fun highestScenicScore(treeGrid: Array<Array<Int>>): Int {
    // don't consider edges because they are 0
    val minY = 1
    val maxY = treeGrid.size - 1
    val minX = 1
    val maxX = treeGrid[0].size - 1
    var maxScenicScore = 0
    for (y in minY until maxY) {
        for (x in minX until maxX) {
            val currTree = treeGrid[y][x]
            val yUpRange = (y - 1 downTo 0)
            val yDownRange = (y + 1 until treeGrid.size)
            val xLeftRange = (x - 1 downTo 0)
            val xRightRange = (x + 1 until treeGrid[0].size)
            val tallerUpTreeDistance = yUpRange.firstOrNull { treeGrid[it][x] >= currTree }?.let { y - it } ?: (y - 0)
            val tallerDownTreeDistance = yDownRange.firstOrNull { treeGrid[it][x] >= currTree }?.let { it - y } ?: (treeGrid.size - 1 - y)
            val tallerLeftTreeDistance = xLeftRange.firstOrNull { treeGrid[y][it] >= currTree }?.let { x - it } ?: (x - 0)
            val tallerRightTreeDistance = xRightRange.firstOrNull { treeGrid[y][it] >= currTree }?.let { it - x } ?: (treeGrid[0].size - 1 - x)
            maxScenicScore = maxOf(maxScenicScore, scenicScore(tallerUpTreeDistance, tallerDownTreeDistance, tallerLeftTreeDistance, tallerRightTreeDistance))
        }
    }
    return maxScenicScore
}

fun scenicScore(distanceToUpTree: Int, distanceToDownTree: Int, distanceToLeftTree: Int, distanceToRightTree: Int): Int {
    return distanceToUpTree * distanceToDownTree * distanceToLeftTree * distanceToRightTree
}
