package search

import java.io.File

class SearchEngine {
    var dataSets = mutableMapOf<Int, String>()
    var invertedIndex = mutableMapOf<String, MutableList<Int>>()
    var mode = SearchEngineMode.UNDEFINED
    var strategy = SearchStrategy.UNDEFINED
    var searchInput: String = ""

    fun askUser() {
        println("=== Menu ===")
        println("1. Find a person")
        println("2. Print all people")
        println("0. Exit")
        val input = try {
            readln().toInt()
        } catch (e: java.lang.Exception) {
            -1
        }
        mode = when (input) {
            1 -> SearchEngineMode.FIND_PERSON
            2 -> SearchEngineMode.PRINT_ALL_DATA
            0 -> SearchEngineMode.EXIT
            else -> SearchEngineMode.UNDEFINED
        }
        println()
        executeTask(mode)
    }

    private fun executeTask(mode: SearchEngineMode) {
        when (mode) {
            SearchEngineMode.FIND_PERSON -> executeFindPerson()
            SearchEngineMode.PRINT_ALL_DATA -> executePrintAllData()
            SearchEngineMode.EXIT -> executeExit()
            SearchEngineMode.UNDEFINED -> executeUndefined()
        }
    }

    private fun executeExit() {
        println("Bye!")
    }

    private fun executeFindPerson() {
        println("Select a matching strategy: ALL, ANY, NONE")
        val strategyInput = readln()
        strategy = when (strategyInput) {
            "ANY" -> SearchStrategy.ANY
            "ALL" -> SearchStrategy.ALL
            "NONE" -> SearchStrategy.NONE
            else -> SearchStrategy.UNDEFINED
        }
        println()
        println("Enter a name or email to search all suitable people.")
        searchInput = readln()
        val results = executeSearch()
        printResults(results)
    }

    private fun printResults(results: List<String>) {
        if (results.isEmpty()) {
            println("No matching people found.")
        } else {
            println("${results.size} person${if (results.size > 1) "s" else ""} found:")
            results.forEach { println(it) }
        }
    }

    private fun executeSearch(): List<String> {
        val searchTerms = searchInput.split(Regex("\\s"))
        return when(strategy) {
            SearchStrategy.ALL -> executeAllSearch(searchTerms)
            SearchStrategy.ANY -> executeAnySearch(searchTerms)
            SearchStrategy.NONE -> executeNoneSearch(searchTerms)
            else -> listOf()
        }
    }

    private fun executeNoneSearch(terms: List<String>): List<String> {
        val results = mutableListOf<String>()
        val indicesPerTerm = fetchIndicesFor(terms)
        val allFoundIndices = indicesPerTerm.values.flatMap { it.toSet() }.toSet()
        val allIndices = dataSets.keys
        allIndices.removeAll(allFoundIndices)
        allIndices.forEach { index -> dataSets[index]?.let { results.add(it) } }
        return results
    }

    private fun executeAnySearch(terms: List<String>): List<String> {
        val results = mutableListOf<String>()
        val indicesPerTerm = fetchIndicesFor(terms)
        val allFoundIndices = indicesPerTerm.values.flatMap { it.toSet() }.toSet()
        allFoundIndices.forEach { index -> dataSets[index]?.let { results.add(it) } }
        return results
    }

    private fun executeAllSearch(terms: List<String>): List<String> {
        val results = mutableListOf<String>()
        val indicesPerTerm = fetchIndicesFor(terms)
        val allFoundIndices = indicesPerTerm.values.flatMap { it.toSet() }.toSet()
        for (foundIndex in allFoundIndices) {
            var indexContainedInAllIndices = true
            for ((_, indices) in indicesPerTerm) {
                if (!indices.contains(foundIndex)) {
                    indexContainedInAllIndices = false
                    break
                }
            }
            if (indexContainedInAllIndices) {
                dataSets[foundIndex]?.let { results.add(it) }
            }
        }
        return results
    }

    private fun fetchIndicesFor(terms: List<String>): Map<String, List<Int>> {
        val indicesPerTerm = mutableMapOf<String, MutableList<Int>>()
       for (term in terms) {
           indicesPerTerm[term.lowercase()] = invertedIndex[term.lowercase()] ?: mutableListOf()
       }
        return indicesPerTerm.filterValues { it.isNotEmpty() }
    }

    private fun executePrintAllData() {
        println("=== List of people ===")
        dataSets.forEach { println(it.value) }
        println()
    }

    private fun executeUndefined() {
        println("Incorrect option! Try again.")
        println()
    }

    fun readData(path: String) {
        val dataFile = File(path)
        var index = 0
        dataFile.readLines().forEach {
            dataSets[index] = it
            index++
        }
        rebuildInvertedIndex()
    }

    private fun rebuildInvertedIndex() {
        invertedIndex.clear()
        for ((lineNo, line) in dataSets) {
            val words = line.split(Regex("\\s"))
            for (word in words) {
                invertedIndex.getOrPut(word.lowercase()) { mutableListOf() }.add(lineNo)
            }
        }
    }
}