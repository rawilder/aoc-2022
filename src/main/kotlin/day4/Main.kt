package day4

import util.Util
import java.lang.RuntimeException

fun main() {
    val input = Util.resourcesFile("/day4/input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedInput = input.split("\n").filterNot { it.isEmpty() }
    val ranges: List<Pair<IntRange, IntRange>> = arrayifiedInput.map { it.split(",") }
        .map { it.map { it.split("-").map { it.toInt() } } }
        .map { it.map { it[0]..it[1] } }
        .map { it[0] to it[1] }
    ranges.count {
        it.first.intersect(it.second).size == it.second.count() || it.second.intersect(it.first).size == it.first.count()
    }.also {
        println("star 1: $it")
    }

    ranges.count {
        it.first.intersect(it.second).isNotEmpty() || it.second.intersect(it.first).isNotEmpty()
    }.also {
        println("star 2: $it")
    }
}
