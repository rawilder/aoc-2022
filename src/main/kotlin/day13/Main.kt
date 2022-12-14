package day13

import util.Util
import util.Util.shouldBe
import kotlin.math.max

fun main() {
    val testPackets = parseFileToPairsOfPackets("/day13/test_input.txt")
    testPackets.withIndex().filter {
        packetsInRightOrder(it.value.first, it.value.second)
    }.sumOf {
        it.index + 1
    }.also {
        it.shouldBe(13)
    }

    val dividerPacket2 = parsePacket("[[2]]")
    val dividerPacket6 = parsePacket("[[6]]")
    val testPacketsWithDivider = testPackets.flatMap { listOf(it.first, it.second) } + dividerPacket2 + dividerPacket6
    val testPacketsWithDividerSorted = testPacketsWithDivider.sorted()
    testPacketsWithDividerSorted.withIndex().filter {
        it.value in listOf(dividerPacket2, dividerPacket6)
    }.map {
        it.index + 1
    }.reduce { acc, i ->
        acc * i
    }.also {
        it.shouldBe(140)
    }


    val packets = parseFileToPairsOfPackets("/day13/input.txt")
    packets.withIndex().filter {
        packetsInRightOrder(it.value.first, it.value.second)
    }.sumOf {
        it.index + 1
    }.also {
        println(it)
    }

    // add divider packets [[2]],[[6]]
    val packetsWithDivider = packets.flatMap { listOf(it.first, it.second) } + dividerPacket2 + dividerPacket6
    val packetsWithDividerSorted = packetsWithDivider.sorted()
    // find divider packets and multiply their index
    packetsWithDividerSorted.withIndex().filter {
        it.value in listOf(dividerPacket2, dividerPacket6)
    }.map {
        it.index + 1
    }.reduce { acc, i ->
        acc * i
    }.also {
        println(it)
    }
}

sealed class IntOrList {
    class Int(val int: kotlin.Int) : IntOrList()
    class List(val list: kotlin.collections.List<IntOrList>) : IntOrList()
}

data class Packet(
    val data: IntOrList.List
) : Comparable<Packet> {
    override fun compareTo(other: Packet): Int {
        return packetComparison(this.data, other.data)
    }
}

fun parseFileToPairsOfPackets(filename: String): List<Pair<Packet, Packet>> {
    val lines = Util.resourcesFile(filename)?.readText()!!.lines().filter { it.isNotBlank() }
    val packets = mutableListOf<Pair<Packet, Packet>>()
    var i = 0
    while (i < lines.size) {
        val packet1 = parsePacket(lines[i])
        val packet2 = parsePacket(lines[i + 1])
        packets.add(packet1 to packet2)
        i += 2
    }
    return packets
}

fun parsePacket(line: String): Packet {
    return Packet(data = parseIntOrList(line.toCharArray().toMutableList()) as IntOrList.List)
}

fun parseIntOrList(line: MutableList<Char>): IntOrList {
    val list = mutableListOf<IntOrList>()
    while(line.isNotEmpty()) {
        val char = line.removeFirst()
        // if first char is [ then it's a list
        if (char == '[') {
            list.add(parseIntOrList(line))
        }
        if (char == ']') {
            return IntOrList.List(list)
        }
        if (char.isDigit()) {
            var token = char.toString()
            while (line.isNotEmpty() && line.first().isDigit()) {
                token += line.removeFirst()
            }
            list.add(IntOrList.Int(token.toInt()))
        }
    }
    // this is the exit case in which the list will always be size 1
    // (with proper input)
    return list.first()
}

fun packetsInRightOrder(left: Packet, right: Packet): Boolean {
    return packetComparison(left.data, right.data) < 0
}

fun packetComparison(left: IntOrList, right: IntOrList): Int {
    when {
        left is IntOrList.Int && right is IntOrList.Int -> {
            return left.int.compareTo(right.int)
        }
        left is IntOrList.List || right is IntOrList.List -> {
            val leftAsList = left as? IntOrList.List ?: IntOrList.List(listOf(left))
            val rightAsList = right as? IntOrList.List ?: IntOrList.List(listOf(right))
            for (i in 0 until max(leftAsList.list.size, rightAsList.list.size)) {
                val leftNext = leftAsList.list.getOrNull(i) ?: break
                val rightNext = rightAsList.list.getOrNull(i) ?: break
                val result = packetComparison(leftNext, rightNext)
                if (result != 0) {
                    return result
                }
            }
            return leftAsList.list.size.compareTo(rightAsList.list.size)
        }
    }
    return 1
}
