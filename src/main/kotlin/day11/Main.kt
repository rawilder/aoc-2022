package day11

import util.Util
import java.lang.RuntimeException

fun main() {
    val testResult = calculateMonkeyBusiness(parseFileToMonkeys("/day11/test_input.txt"), 20, 3)
    require(testResult == 10605L) {
        "test failed: $testResult != 10605"
    }
    val star2TestResult = calculateMonkeyBusiness2ElectricBoogaloo(parseFileToMonkeys("/day11/test_input.txt"), 10000)
    require(star2TestResult == 2713310158) {
        "star2 test failed: $star2TestResult != 2713310158"
    }

    val monkeys = parseFileToMonkeys("/day11/input.txt")
    println("star 1: ${calculateMonkeyBusiness(monkeys, 20, 3)}")
    println("star 2: ${calculateMonkeyBusiness2ElectricBoogaloo(monkeys, 10000)}")
}

object MonkeyRegex {
    val monkeyNumber = Regex("Monkey (\\d):")
    val startingItems = Regex("Starting items: (.*)")
    val operation = Regex("Operation: new = old ([+*]) (\\d+|old)")
    val test = Regex("Test: divisible by (\\d+)")
    val ifTrue = Regex("If true: throw to monkey (\\d+)")
    val ifFalse = Regex("If false: throw to monkey (\\d+)")
}

data class Monkey(
    val number: Int,
    val items: MutableList<Long>,
    val operation: (Long) -> Long,
    val op: OP,
    val operand: Operand,
    val testNumber: Long,
    val ifTrueMonkeyNumber: Int,
    val ifFalseMonkeyNumber: Int
)

enum class OP {
    ADD,
    MULTIPLY
}

enum class OPERAND_TYPE {
    OLD,
    NUMBER
}

data class Operand(
    val type: OPERAND_TYPE,
    val value: Long?
)

fun parseFileToMonkeys(file: String): Map<Int, Monkey> {
    return Util.resourcesFile(file)?.readText()?.split("\n\n")
        ?.filterNot { it.isEmpty() }?.associate { monkeyBlock ->
            val monkeyNumber = MonkeyRegex.monkeyNumber.find(monkeyBlock)?.groupValues?.get(1)?.toInt()
                ?: throw RuntimeException("no monkey number found")
            val startingItems: List<Long> = MonkeyRegex.startingItems.find(monkeyBlock)?.groupValues?.let {
                it[1].split(",").map { it.trim().toLong() }
            } ?: throw RuntimeException("no starting items found")
            val operation = MonkeyRegex.operation.find(monkeyBlock)?.groupValues?.let { operation ->
                val operator = operation[1]
                val operand = if (operation[2] != "old") operation[2].toLong() else null
                when (operator) {
                    "+" -> { i: Long -> i + (operand ?: i) }
                    "*" -> { i: Long -> i * (operand ?: i) }
                    else -> throw RuntimeException("unknown operator $operator")
                }
            } ?: throw RuntimeException("no operation found")
            val (op, operand) = MonkeyRegex.operation.find(monkeyBlock)?.groupValues?.let { operation ->
                val operator = operation[1]
                val operand = if (operation[2] != "old") operation[2].toLong() else null
                when (operator) {
                    "+" -> OP.ADD to Operand(
                        if (operand == null) OPERAND_TYPE.OLD else OPERAND_TYPE.NUMBER,
                        operand
                    )
                    "*" -> OP.MULTIPLY to Operand(
                        if (operand == null) OPERAND_TYPE.OLD else OPERAND_TYPE.NUMBER,
                        operand
                    )
                    else -> throw RuntimeException("unknown operator $operator")
                }
            } ?: throw RuntimeException("no operation found")
            val test = MonkeyRegex.test.find(monkeyBlock)?.destructured?.component1()?.toLong()!!
            val ifTrueMonkeyNumber = MonkeyRegex.ifTrue.find(monkeyBlock)?.destructured?.component1()?.toInt()!!
            val ifFalseMonkeyNumber = MonkeyRegex.ifFalse.find(monkeyBlock)?.destructured?.component1()?.toInt()!!

            monkeyNumber to Monkey(
                monkeyNumber,
                startingItems.toMutableList(),
                operation,
                op,
                operand,
                test,
                ifTrueMonkeyNumber,
                ifFalseMonkeyNumber
            )
        } ?: throw RuntimeException("no monkeys found")
}

fun lowestCommonDenominator(a: Long, b: Long): Long {
    var a = a
    var b = b
    while (a != 0L && b != 0L) {
        val c = a
        a = b % a
        b = c
    }
    return b + a
}

fun calculateMonkeyBusiness(monkeys: Map<Int, Monkey>, numberOfRounds: Int, reliefDivisor: Long): Long {
    val numberInspections: MutableMap<Int, Int> = monkeys.keys.associateWith { 0 }.toMutableMap()
    repeat(numberOfRounds) {
        monkeys.forEach { (_, monkey) ->
            while (monkey.items.isNotEmpty()) {
                val worryLevel = monkey.items.removeAt(0)
                val elevatedWorryLevel = monkey.operation(worryLevel)
                val calmedWorryLevel = elevatedWorryLevel / reliefDivisor
                if (calmedWorryLevel % monkey.testNumber == 0L) {
                    monkeys[monkey.ifTrueMonkeyNumber]?.items?.add(calmedWorryLevel)
                } else {
                    monkeys[monkey.ifFalseMonkeyNumber]?.items?.add(calmedWorryLevel)
                }
                numberInspections[monkey.number] = numberInspections[monkey.number]!! + 1
            }
        }
    }
    return numberInspections.values.sortedDescending().take(2).let { it[0].toLong() * it[1] }
}

fun calculateMonkeyBusiness2ElectricBoogaloo(monkeys: Map<Int, Monkey>, numberOfRounds: Int): Long {
    val numberInspections: MutableMap<Int, Int> = monkeys.keys.associateWith { 0 }.toMutableMap()
    repeat(numberOfRounds) {
        monkeys.forEach { (_, monkey) ->
            while (monkey.items.isNotEmpty()) {
                val worryLevel = monkey.items.removeAt(0)
                val elevatedWorryLevel = monkey.operation(worryLevel)
                val calmedWorryLevel = elevatedWorryLevel
                if (elevatedWorryLevel % monkey.testNumber == 0L) {
                    monkeys[monkey.ifTrueMonkeyNumber]?.items?.add(calmedWorryLevel)
                } else {
                    monkeys[monkey.ifFalseMonkeyNumber]?.items?.add(calmedWorryLevel)
                }
                numberInspections[monkey.number] = numberInspections[monkey.number]!! + 1
            }
        }
    }
    return numberInspections.values.sortedDescending().take(2).let { it[0].toLong() * it[1] }
}
