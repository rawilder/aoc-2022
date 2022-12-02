package day1

import util.Util
import java.lang.RuntimeException

fun main() {
    val step1 = Util.resourcesFile("/day1/input.txt")?.readText()?.let { input ->
        input.split("\n\n").map { elfSnacks ->
            elfSnacks.split("\n").filterNot { snack -> snack.isEmpty() }.sumOf { snack -> snack.toInt() }
        }
    } ?: throw RuntimeException("input not found")

    println("star 1: ${step1.max()}")
    println("star 2: ${step1.sortedDescending().take(3).sum()}")
}
