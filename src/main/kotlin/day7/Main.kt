package day7

import util.Util
import java.lang.RuntimeException

fun main() {
    val testInput = Util.resourcesFile("/day7/test_input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedTestInput = testInput.split("\n").filterNot { it.isEmpty() }.toMutableList()
    val testRoot = DirectoryEntry("/", null)
    parseOutput(arrayifiedTestInput.drop(1), testRoot)
    val testResult = sizeOfDirectoriesAtMostN(100000, testRoot)
    require(testResult == 95437) {
        "test failed: $testResult != 95437"
    }
    val deleteTestResult = findDirectoryToDelete(70000000, 30000000, testRoot)
    require(deleteTestResult == 24933642) {
        "test failed: $deleteTestResult != 24933642"
    }

    val input = Util.resourcesFile("/day7/input.txt")?.readText() ?: throw RuntimeException("input not found")
    val arrayifiedInput = input.split("\n").filterNot { it.isEmpty() }.toMutableList()
    val root = DirectoryEntry("/", null)
    parseOutput(arrayifiedInput.drop(1), root)
    sizeOfDirectoriesAtMostN(100000, root).also {
        println("star 1: $it")
    }
    findDirectoryToDelete(70000000, 30000000, root).also {
        println("star 2: $it")
    }
}

// star 1
fun sizeOfDirectoriesAtMostN(n: Int, directoryEntry: DirectoryEntry): Int {
    return if (directoryEntry.size == null) {
        directoryEntry.totalSize().atMost(n) + directoryEntry.children.map { sizeOfDirectoriesAtMostN(n, it) }.sum()
    } else {
        0
    }
}

fun Int.atMost(n: Int) = if (this <= n) this else 0

fun parseOutput(output: List<String>, currentDirectory: DirectoryEntry): DirectoryEntry {
    if (output.isEmpty()) {
        return currentDirectory
    }
    val command = output.first().split(" ")[1]
    val args = output.first().split(" ").drop(2)
    when (command) {
        "cd" -> {
            return when {
                args[0] == ".." -> {
                    // .. will only happen on non-root
                    parseOutput(output.drop(1), currentDirectory.parent!!)
                }

                else -> {
                    parseOutput(
                        output.drop(1),
                        currentDirectory.children.firstOrNull { it.name == args[0] } ?: throw RuntimeException("directory not found")
                    )
                }
            }
        }
        "ls" -> {
            val children = output.drop(1).takeWhile { it[0] != '$' }.map {
                val props = it.split(" ")
                val name = props[1]
                val size = props[0].toIntOrNull()
                DirectoryEntry(name, size, currentDirectory)
            }
            return parseOutput(output.drop(1 + children.size), currentDirectory.also { it.children.addAll(children) })
        }
        else -> throw RuntimeException("unknown command: $command")
    }
}

data class DirectoryEntry(val name: String, val size: Int? = null, val parent: DirectoryEntry? = null, var children: MutableList<DirectoryEntry> = mutableListOf()) {

    private var cachedSize: Int? = null

    fun totalSize(): Int {
        return if (cachedSize != null) {
            cachedSize!!
        } else {
            (size ?: children.sumOf { it.totalSize() }).also {
                cachedSize = it
            }
        }
    }
}

// star 2
// returns size of directory to delete to get requisite space
fun findDirectoryToDelete(totalSpace: Int, requiredSpace: Int, directoryEntry: DirectoryEntry): Int {
    val usedSpace = directoryEntry.totalSize()
    val freeSpace = totalSpace - usedSpace
    return if (freeSpace >= requiredSpace) {
        0
    } else {
        val minimumDeleteSpace = requiredSpace - freeSpace
        sizeOfDirectoryAtMinimumN(minimumDeleteSpace, directoryEntry.totalSize(), directoryEntry)
    }
}

fun sizeOfDirectoryAtMinimumN(n: Int, minSizeSoFar: Int, directoryEntry: DirectoryEntry): Int {
    return if (directoryEntry.size == null) {
        val dirSize = directoryEntry.totalSize()
        return if (dirSize < n) {
            minSizeSoFar
        } else if (dirSize >= minSizeSoFar) {
            var minSizeSoFarInternal = minSizeSoFar
            directoryEntry.children.minOfOrNull { sizeOfDirectoryAtMinimumN(n, minSizeSoFarInternal, it).also { minSizeSoFarInternal = it } } ?: throw RuntimeException("no children")
        } else {
            dirSize
        }
    } else {
        minSizeSoFar
    }
}
