package search

fun main(args: Array<String>) {
    val searchEngine = SearchEngine()
    searchEngine.readData(args[1])
    do {
        searchEngine.askUser()
    } while (searchEngine.mode != SearchEngineMode.EXIT)
}
