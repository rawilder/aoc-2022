package day12

import util.Util
import java.lang.RuntimeException

fun main() {
    val graphNodeMap = parseFileToGraph("/day12/test_input.txt")
    val testResult = dijkstra(graphNodeMap, 'S', 'E')
    require(testResult == 31) {
        "test failed: $testResult != 31"
    }

    println("star 1: ${dijkstra(parseFileToGraph("/day12/input.txt"), 'S', 'E')}")
    val input = parseFileToGraph("/day12/input.txt")
    // kinda sad i just brute forced this
    val star2Result = (0 until 40).map {
        dijkstraStar2(input, 0 to it, 'E')
    }.min()
    println("star 2: $star2Result")
}

data class GraphNode(
    val value: Char,
    val neighbors: MutableList<Pair<Int, Int>>
)

fun parseFileToGraph(filename: String): Map<Pair<Int, Int>, GraphNode> {
    val graphNodeMatrix = (Util.resourcesFile(filename)?.readText()?.split("\n")?.filter { it.isNotEmpty() }?: throw RuntimeException("file not found"))
        .map {
            it.toList().map {
                GraphNode(it, mutableListOf())
            }
        }

    val graphNodeMap = mutableMapOf<Pair<Int, Int>, GraphNode>()
    graphNodeMatrix.forEachIndexed { y, row ->
        row.forEachIndexed { x, graphNode ->
            graphNodeMap[Pair(x, y)] = graphNode
        }
    }
    // assign neighbors
    graphNodeMap.forEach { (pos, graphNode) ->
        val (x, y) = pos
        val neighbors = mutableListOf<Pair<Int, Int>>()
        if (x > 0) {
            neighbors.add(Pair(x - 1, y))
        }
        if (x < graphNodeMatrix[0].size - 1) {
            neighbors.add(Pair(x + 1, y))
        }
        if (y > 0) {
            neighbors.add(Pair(x, y - 1))
        }
        if (y < graphNodeMatrix.size - 1) {
            neighbors.add(Pair(x, y + 1))
        }
        graphNode.neighbors.addAll(neighbors.filter { graphNode.allowedToMoveTo(graphNodeMap[it]!!) })
    }

    return graphNodeMap
}

fun GraphNode.allowedToMoveTo(node: GraphNode): Boolean {
    val source = getWeight(this.value)
    val target = getWeight(node.value)
    return when (source) {
        'a' -> {
            target in 'a'..source + 1
        }
        'z' -> {
            target in 'a'..source
        }
        else -> {
            target in 'a' .. source + 1
        }
    }
}

fun getWeight(value: Char): Char {
    return when (value) {
        'S' -> 'a'
        'E' -> 'z'
        else -> value
    }
}

fun dijkstra(graph: Map<Pair<Int, Int>, GraphNode>, sourceChar: Char, targetChar: Char): Int {
    val unvisited = graph.values.toMutableList()
    val source = unvisited.find { it.value == sourceChar } ?: throw RuntimeException("source not found")
    val target = unvisited.find { it.value == targetChar } ?: throw RuntimeException("target not found")
    val distances: MutableMap<GraphNode, Int> = unvisited.associateWith { Int.MAX_VALUE }.toMutableMap()
    val previous: MutableMap<GraphNode, GraphNode?> = unvisited.associateWith { null }.toMutableMap()

    distances[source] = 0
    previous[source] = null

    while (unvisited.isNotEmpty()) {
        val current = unvisited.minByOrNull { distances[it]!! } ?: throw RuntimeException("no min found")
        unvisited.remove(current)

        for (neighbor in current.neighbors.map { graph[it]!! }) {
            val alt = (distances[current]!!) + 1
            if (alt < (distances[neighbor]!!)) {
                distances[neighbor] = alt
                previous[neighbor] = current
            }
        }
    }

    return distances[target]!!
}

fun dijkstraStar2(graph: Map<Pair<Int, Int>, GraphNode>, sourceCoords: Pair<Int, Int>, targetChar: Char): Int {
    val unvisited = graph.values.toMutableList()
    val source = graph[sourceCoords]!!
    val target = unvisited.find { it.value == targetChar } ?: throw RuntimeException("target not found")
    val distances: MutableMap<GraphNode, Int> = unvisited.associateWith { Int.MAX_VALUE }.toMutableMap()
    val previous: MutableMap<GraphNode, GraphNode?> = unvisited.associateWith { null }.toMutableMap()

    distances[source] = 0
    previous[source] = null

    while (unvisited.isNotEmpty()) {
        val current = unvisited.minByOrNull { distances[it]!! } ?: throw RuntimeException("no min found")
        unvisited.remove(current)

        for (neighbor in current.neighbors.map { graph[it]!! }) {
            val alt = (distances[current]!!) + 1
            if (alt < (distances[neighbor]!!)) {
                distances[neighbor] = alt
                previous[neighbor] = current
            }
        }
    }

    return distances[target]!!
}
