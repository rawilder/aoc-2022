package day3

import util.Util
import java.lang.RuntimeException

fun score(char: Char): Int {
    return when {
        char.isUpperCase() -> char.lowercaseChar().code - 96 + 26
        else -> char.code - 96
    }
}

fun findCommonChar(first: String, vararg rest: String): Char {
    return first.toSet().intersect(rest.map { it.toSet() }.reduce { acc, set -> acc.intersect(set) }).first()
}

fun main() {
    val input = Util.resourcesFile("/day3/input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedInput = input.split("\n").filterNot { it.isEmpty() }
    arrayifiedInput.map { it.substring(0, it.length / 2) to it.substring(it.length / 2, it.length) }.sumOf {
            val firstCommonChar = findCommonChar(it.first, it.second)
            val score = score(firstCommonChar)
            score
        }.also {
            println("star 1: $it")
        }
    arrayifiedInput.chunked(3).sumOf {
            val firstCommonChar = findCommonChar(it[0], it[1], it[2])
            val score = score(firstCommonChar)
            score
        }.also {
            println("star 2: $it")
        }
}
