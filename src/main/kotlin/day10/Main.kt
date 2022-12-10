package day10

import util.Util
import java.lang.RuntimeException

var x = 1

fun main() {
    val testResult = runInstructions(parseFileToInstructions("/day10/test_input.txt"), 20, 40)
    require(testResult == 13140) {
        "test failed: $testResult != 13140"
    }
    drawInstructions(parseFileToInstructions("/day10/test_input.txt"), 40)

    val instructions = parseFileToInstructions("/day10/input.txt")
    println("star 1: ${runInstructions(instructions, 20, 40)}")
    println("star 2\n:")
    drawInstructions(instructions, 40)
}

fun parseFileToInstructions(file: String): List<InstructionAndArgs> {
    return Util.resourcesFile(file)?.readText()?.split("\n")
        ?.filterNot { it.isEmpty() }
        ?.map { it.split(" ").let { InstructionAndArgs(it[0], it.drop(1)) } }
        ?: throw RuntimeException("input not found")
}

data class InstructionAndArgs(val instruction: Instruction, val args: List<String>) {
    constructor(instruction: String, args: List<String>) : this(Instruction.fromCode(instruction), args)
}

enum class Instruction(val code: String, val numCycles: Int, val handleArgs: (List<String>) -> Unit) {
    NOOP("noop", 1, {}),
    ADDX("addx", 2, { x += it[0].toInt() });

    companion object {
        fun fromCode(code: String): Instruction {
            return values().firstOrNull { it.code == code } ?: throw RuntimeException("invalid code: $code")
        }
    }
}

// star 1
fun runInstructions(instructions: List<InstructionAndArgs>, startCycle: Int, cycleInterval: Int): Int {
    var cycles = 0
    var sum = 0
    instructions.forEach { instructionAndArgs ->
        var internalCycles = 0
        while (internalCycles < instructionAndArgs.instruction.numCycles) {
            internalCycles++
            cycles++
            if ((cycles - startCycle) % cycleInterval == 0) {
                sum += x * cycles
            }
        }
        instructionAndArgs.instruction.handleArgs(instructionAndArgs.args)
    }
    x = 1
    return sum
}

// star 2
fun drawInstructions(instructions: List<InstructionAndArgs>, cycleInterval: Int) {
    var cycles = 0
    var previousRow = 0
    instructions.forEach { instructionAndArgs ->
        var internalCycles = 0
        while (internalCycles < instructionAndArgs.instruction.numCycles) {
            if (previousRow != cycles / cycleInterval) {
                println()
                previousRow = cycles / cycleInterval
            }
            if ((cycles % cycleInterval) in x-1..x+1) {
                print("#")
            } else print(".")
            internalCycles++
            cycles++
        }
        instructionAndArgs.instruction.handleArgs(instructionAndArgs.args)
    }
    x = 1
    println()
}
