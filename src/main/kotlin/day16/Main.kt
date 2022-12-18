package day16

import util.Util
import util.Util.shouldBe
import kotlin.math.max
import kotlin.system.measureTimeMillis

fun main() {
    val testGraph = parseFileToGraph("/day16/test_input.txt")
    star1(testGraph, "AA", 30) shouldBe 1651
    measureTimeMillis {
        star2(testGraph, "AA", "AA", 26, 26) shouldBe 1707
    }.also { println("Test Star 2 took $it ms") }

    val graph = parseFileToGraph("/day16/input.txt")
    println("star 1: ${star1(graph, "AA", 30)}")
    println("star 2: ${star2(graph, "AA", "AA", 26, 26)}")
}

fun parseFileToGraph(resourcePath: String): Graph {
    val lines = Util.resourcesFile(resourcePath)?.readText()?.lines()?.filter { it.isNotEmpty() } ?: throw IllegalArgumentException("No file found at $resourcePath")
    return lines.fold(Graph(emptyMap(), emptyMap())) { graph, line ->
        val (node, neighbors) = line.split("; ").let {
            Graph.Valve(it[0].split(" ")[1], it[0].split("=")[1].toInt()) to
                it[1].split(Regex(" "), 5).last().split(", ").toSet()
    }
        graph.copy(
            valves = graph.valves + (node.valveName to node),
            tunnels = graph.tunnels.entries.fold(mapOf(node.valveName to neighbors)) { acc, entry ->
                acc + (entry.key to (acc[entry.key] ?: emptySet()) + entry.value)
            }
        )
    }
}

data class Graph(val valves: Map<String, Valve>, val tunnels: Map<String, Set<String>>) {
    data class Valve(val valveName: String, val flowRate: Int)
}

tailrec fun star1(
    graph: Graph,
    currentValve: String,
    minutes: Int,
    pressureReleased: Int = 0,
    unopened: Set<String> = graph.valves.filterValues { valve -> valve.flowRate > 0 }.keys,
    allDistances: Map<String, Map<String, Int>> = graph.valves.keys.associateWith { dijkstras(graph, it) }
): Int {
    val validUnopened = unopened.filter { allDistances[currentValve]!![it]!! + 1 <= minutes }
    return if (validUnopened.isEmpty() || minutes <= 0)
        pressureReleased
    else
        validUnopened.filter { allDistances[currentValve]!![it]!! + 1 <= minutes }.maxOf {
            val minutesTakenToMoveAndOpen = allDistances[currentValve]!![it]!! + 1
            val minutesLeft = minutes - minutesTakenToMoveAndOpen
            star1(graph, it, minutesLeft, pressureReleased + graph.valves[it]!!.flowRate * minutesLeft, unopened - it, allDistances)
        }
}

tailrec fun star2(
    graph: Graph,
    myCurrentValve: String?,
    elephantCurrentValve: String?,
    myMinutes: Int,
    elephantMinutes: Int,
    pressureReleased: Int = 0,
    unopened: Set<String> = graph.valves.filterValues { valve -> valve.flowRate > 0 }.keys,
    allDistances: Map<String, Map<String, Int>> = graph.valves.keys.associateWith { dijkstras(graph, it) }
): Int {

    val myValidUnopened = unopened.filter { (allDistances[myCurrentValve]?.get(it) ?: Int.MAX_VALUE) + 1 <= myMinutes }
    val elephantValidUnopened = unopened.filter { (allDistances[elephantCurrentValve]?.get(it) ?: Int.MAX_VALUE) + 1 <= elephantMinutes }
    val allPossibleValveCombinations: List<Pair<String?, String?>> = if (myValidUnopened.isEmpty()) {
        elephantValidUnopened.map { null to it }
    } else if (elephantValidUnopened.isEmpty()) {
        myValidUnopened.map { it to null }
    } else {
        myValidUnopened.flatMap { myValve -> elephantValidUnopened.map { myValve to it } }.filter { it.first != it.second }
    }
    return if ((myValidUnopened.isEmpty() || myMinutes <= 0) && (elephantValidUnopened.isEmpty() || elephantMinutes <= 0) || allPossibleValveCombinations.isEmpty())
        pressureReleased
    else {
        allPossibleValveCombinations.fold(-1) { acc, (maybeMyValve, maybeElephantsValve) ->
            // prune if we can't beat the current best
            val bestCasePressureRelease = unopened.sortedByDescending { graph.valves[it]!!.flowRate }.fold(Pair(0, max(myMinutes, elephantMinutes))) { (acc, minutes), unopenedValve ->
                graph.valves[unopenedValve]!!.flowRate * minutes + acc to minutes - 1
            }.first
            if (acc >= pressureReleased + bestCasePressureRelease) {
                acc
            } else {
                val myMinutesTakenToMoveAndOpen = myCurrentValve?.let { myCurrentValve -> allDistances[myCurrentValve]?.get(maybeMyValve)?.plus(1) ?: 0 } ?: 0
                val elephantMinutesTakenToMoveAndOpen = elephantCurrentValve?.let { elephantCurrentValve -> allDistances[elephantCurrentValve]?.get(maybeElephantsValve)?.plus(1) ?: 0 } ?: 0
                val myMinutesLeft = myMinutes - myMinutesTakenToMoveAndOpen
                val elephantMinutesLeft = elephantMinutes - elephantMinutesTakenToMoveAndOpen
                val myPressure = maybeMyValve?.let { graph.valves[it]!!.flowRate * myMinutesLeft } ?: 0
                val elephantPressure = maybeElephantsValve?.let { graph.valves[it]!!.flowRate * elephantMinutesLeft } ?: 0
                max(
                    acc,
                    star2(
                        graph,
                        maybeMyValve,
                        maybeElephantsValve,
                        myMinutesLeft,
                        elephantMinutesLeft,
                        pressureReleased + myPressure + elephantPressure,
                        unopened - setOfNotNull(maybeMyValve, maybeElephantsValve),
                        allDistances
                    )
                )
            }
        }
    }
}

tailrec fun dijkstras(
    graph: Graph,
    startingValve: String,
    unvisited: Set<String> = graph.valves.keys,
    distances: Map<String, Int> = graph.valves.keys.associateWith { Int.MAX_VALUE } + (startingValve to 0)
): Map<String, Int> {
    val current = unvisited.minBy { distances[it]!! }
    val neighbors = graph.tunnels[current] ?: emptySet()
    val newDistances = neighbors.fold(distances) { acc, neighbor ->
        val newDistance = distances[current]!! + 1
        if (newDistance < acc[neighbor]!!) {
            acc + (neighbor to newDistance)
        } else {
            acc
        }
    }
    val minDistance = newDistances.filter { it.key in unvisited }.minBy { it.value }
    return when (unvisited.size) {
        1 -> newDistances
        else -> dijkstras(
            graph,
            startingValve,
            unvisited - minDistance.key,
            newDistances - minDistance.key + (minDistance.key to minDistance.value)
        )
    }
}
