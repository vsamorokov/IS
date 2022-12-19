package me.vsamorokov.searchengine.controller

import me.vsamorokov.searchengine.bean.SearchResult
import me.vsamorokov.searchengine.service.Searcher
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Component
class SearcherCli(private val searcher: Searcher) : CommandLineRunner {
    override fun run(vararg args: String?) {
        while (true) {
            try {
                println(answer(searcher.search(validate(readlnOrNull()))))
            } catch (eof: WrongInputException) {
                System.err.println("Wrong input")
            } catch (e: Exception) {
                System.err.println("Something went wrong: " + e.stackTraceToString())
            }
        }
    }
}

@RestController
class SearcherController(private val searcher: Searcher) {
    @GetMapping("/search")
    fun search(@RequestParam words: String): String = answer(searcher.search(validate(words)), true)
}

private fun validate(words: String?): List<String> =
    words?.split(" ")?.also { if (it.size < 2) wie<Nothing>() } ?: wie()


private fun <T> wie(): T = throw WrongInputException

private fun answer(result: SearchResult, html: Boolean = false): String =
    StringBuilder().apply {
        appendLine(result.words.joinToString(transform = { it.second }, prefix = "Words: ", separator = " "))
        appendLine(result.words.joinToString(transform = { it.first.toString() }, prefix = "Ids: ", separator = " "))
        appendLine("${result.matchedUrls} matched Urls found")

        appendLine("%15s | %15s | %15s | %15s | %-100s".format("URL ID", "m1", "m2", "m3", "URL"))
        result.rankings.forEach {
            val (urlId, m1, m2, m3, url) = it
            appendLine("%15d | %15.2f | %15.2f | %15.2f | %-100s".format(urlId, m1, m2, m3, url))
        }

        appendLine()
        val wordsLine = result.table.words.map {
            "%15s | ".format(it)
        }.joinToString(
            prefix = "%15s | ".format("URL ID"),
            postfix = "%-100s".format("URL"),
            separator = ""
        ) { it }
        appendLine(wordsLine)

        result.table.rows.map { row ->
            row.locations.map {
                "%15s | ".format(it)
            }.joinToString(
                prefix = "%15d | ".format(row.url.id),
                postfix = "%-100s".format(row.url.url),
                separator = ""
            ) { it }
        }.forEach {
            appendLine(it)
        }

    }.toString().let {
        if (html) "<pre>$it</pre>" else it
    } + result.contents.joinToString(separator = if (html) "<br><br>" else "\n\n") { it }

private object WrongInputException : RuntimeException()
