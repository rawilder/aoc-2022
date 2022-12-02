package day2

import util.Util
import java.lang.RuntimeException

enum class RPS(val elfCodes: List<Char>, val points: Int) {
    ROCK(listOf('A', 'X'), 1), PAPER(listOf('B', 'Y'), 2), SCISSORS(listOf('C', 'Z'), 3);

    fun gameState(other: RPS): GAME_STATE {
        return when (this) {
            ROCK -> when (other) {
                ROCK -> GAME_STATE.DRAW
                PAPER -> GAME_STATE.LOSE
                SCISSORS -> GAME_STATE.WIN
            }
            PAPER -> when (other) {
                ROCK -> GAME_STATE.WIN
                PAPER -> GAME_STATE.DRAW
                SCISSORS -> GAME_STATE.LOSE
            }
            SCISSORS -> when (other) {
                ROCK -> GAME_STATE.LOSE
                PAPER -> GAME_STATE.WIN
                SCISSORS -> GAME_STATE.DRAW
            }
        }
    }

    fun score(other: RPS): Int = points + gameState(other).points

    companion object {
        val mapOfElfCodeToEnum: Map<Char, RPS> = values().flatMap { rps -> rps.elfCodes.map { elfCode -> elfCode to rps } }.toMap()

        fun ofElfCode(elfCode: Char): RPS {
            return mapOfElfCodeToEnum[elfCode] ?: throw IllegalArgumentException("Invalid elf code: $elfCode")
        }
    }
}

enum class GAME_STATE(val elfCode: Char, val points: Int) {
    WIN('Z', 6), LOSE('X', 0), DRAW('Y', 3);

    companion object {
        val mapOfElfCodeToEnum: Map<Char, GAME_STATE> = values().map { gameState -> gameState.elfCode to gameState }.toMap()

        fun ofElfCode(elfCode: Char): GAME_STATE {
            return mapOfElfCodeToEnum[elfCode] ?: throw IllegalArgumentException("Invalid elf code: $elfCode")
        }
    }
}

fun scorePart1(them: Char, me: Char): Int {
    return RPS.ofElfCode(me).score(RPS.ofElfCode(them))
}

fun scorePart2(them: Char, outcome: Char): Int {
    val expectedGameState = GAME_STATE.ofElfCode(outcome)
    val otherRps = RPS.ofElfCode(them)
    return when (expectedGameState) {
        GAME_STATE.WIN -> {
            when (otherRps) {
                RPS.ROCK -> RPS.PAPER.score(otherRps)
                RPS.PAPER -> RPS.SCISSORS.score(otherRps)
                RPS.SCISSORS -> RPS.ROCK.score(otherRps)
            }
        }
        GAME_STATE.LOSE -> {
            when (otherRps) {
                RPS.ROCK -> RPS.SCISSORS.score(otherRps)
                RPS.PAPER -> RPS.ROCK.score(otherRps)
                RPS.SCISSORS -> RPS.PAPER.score(otherRps)
            }
        }
        GAME_STATE.DRAW -> {
            when (otherRps) {
                RPS.ROCK -> RPS.ROCK.score(otherRps)
                RPS.PAPER -> RPS.PAPER.score(otherRps)
                RPS.SCISSORS -> RPS.SCISSORS.score(otherRps)
            }
        }
    }
}

fun main() {
    val input = Util.resourcesFile("/day2/input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedInput = input.split("\n").filterNot { it.isEmpty() }.map { it.split(" ") }
    val scorePart1 = arrayifiedInput.sumOf { scorePart1(it[0][0], it[1][0]) }
    println("star 1: $scorePart1")
    val scorePart2 = arrayifiedInput.sumOf { scorePart2(it[0][0], it[1][0]) }
    println("star 2: $scorePart2")
}
