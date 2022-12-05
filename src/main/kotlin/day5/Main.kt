package day5

import util.Util
import java.lang.RuntimeException

fun main() {
    val input = Util.resourcesFile("/day5/input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedInput = input.split("\n").filterNot { it.isEmpty() }.toMutableList()
    val crateStrings = mutableListOf<String>()
    while (arrayifiedInput.firstOrNull()?.take(2) != " 1") {
        crateStrings += arrayifiedInput.removeFirst()
    }
    val nums = arrayifiedInput.removeFirst().split(' ').filterNot { it.isEmpty() }.map { it.toInt() }
    val size = nums.last()

    val instructions = arrayifiedInput

    val stacks = createStacks(size, crateStrings)

    val regex = Regex("move ([0-9]+) from ([0-9]+) to ([0-9]+)")

    // star 1
    val star1Stacks = copyStacks(stacks)
    instructions.forEach {
        val (num, from, to) = regex.matchEntire(it)!!.destructured
        repeat(num.toInt()) {
            star1Stacks[to.toInt() - 1].addLast(star1Stacks[from.toInt() - 1].removeLast())
        }
    }
    star1Stacks.fold("") { acc, s -> acc + s.last() }.also { println("star 1: $it") }

    // star 2
    val star2Stacks = copyStacks(stacks)
    instructions.forEach {
        val (num, from, to) = regex.matchEntire(it)!!.destructured
        val movedCrates  = (0 until num.toInt()).map {
            star2Stacks[from.toInt() - 1].removeLast()
        }
        (num.toInt() - 1 downTo 0).map {
            star2Stacks[to.toInt() - 1].addLast(movedCrates[it])
        }
    }
    star2Stacks.fold("") { acc, s -> acc + s.last() }.also { println("star 2: $it") }
}

fun createStacks(size: Int, strings: List<String>): Array<ArrayDeque<Char>> {
    val stacks = Array(size) { ArrayDeque<Char>() }
    strings.forEach {
        var i = 0
        var strCopy = it
        while(strCopy.isNotEmpty()) {
            if (strCopy[0] == '[') {
                stacks[i].addFirst(strCopy[1])
            }
            strCopy = strCopy.drop(4)
            i++
        }
    }
    return stacks
}

fun copyStacks(stacks: Array<ArrayDeque<Char>>): Array<ArrayDeque<Char>> {
    return stacks.map { ArrayDeque(it) }.toTypedArray()
}
