package day6

import util.Util
import java.lang.RuntimeException

fun main() {
    val testInput = Util.resourcesFile("/day6/test_input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedTestInput = testInput.split("\n").filterNot { it.isEmpty() }.toMutableList()
    require(firstOccurrenceOfFourUniqueCharacters(arrayifiedTestInput.first()) == 7)
    require(firstOccurrenceOfUniqueCharactersOfSize(4, arrayifiedTestInput.first()) == 7)

    val input = Util.resourcesFile("/day6/input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedInput = input.split("\n").filterNot { it.isEmpty() }.toMutableList()
    firstOccurrenceOfFourUniqueCharacters(arrayifiedInput.first()).also {
        println("star 1: $it")
    }

    firstOccurrenceOfUniqueCharactersOfSize(14, arrayifiedInput.first()).also {
        println("star 2: $it")
    }
}

// star 1
fun firstOccurrenceOfFourUniqueCharacters(string: String): Int? {
    return string.withIndex().firstOrNull {
        string.substring(it.index, it.index + 4).toSet().size == 4
    }?.index?.plus(4)
}

// star 2
fun firstOccurrenceOfUniqueCharactersOfSize(size: Int, string: String): Int? {
    return string.withIndex().firstOrNull {
        string.substring(it.index, it.index + size).toSet().size == size
    }?.index?.plus(size)
}
