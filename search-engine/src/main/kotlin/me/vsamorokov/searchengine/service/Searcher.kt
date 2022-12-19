package me.vsamorokov.searchengine.service

import me.vsamorokov.data.entity.UrlRecord
import me.vsamorokov.data.entity.WordRecord
import me.vsamorokov.data.repository.WordLocationRepository
import me.vsamorokov.data.repository.WordRecordRepository
import me.vsamorokov.searchengine.bean.PageRankingRow
import me.vsamorokov.searchengine.bean.SearchResult
import me.vsamorokov.searchengine.bean.WordsLocationRow
import me.vsamorokov.searchengine.bean.WordsLocationTable
import me.vsamorokov.searchengine.util.product
import org.springframework.stereotype.Service


@Service
class Searcher(
    private val wordLocationRepository: WordLocationRepository,
    private val wordRecordRepository: WordRecordRepository,
    private val pageRankingService: PageRankingService
) {
    fun search(_words: List<String>): SearchResult {
        val words =
            _words.map {
                wordRecordRepository.findByWord(it.lowercase()) ?: throw IllegalStateException("Word '$it' not found")
            }

        val wordsLocation = words
            .map { wordLocationRepository.findByWordRecord(it) }
            .flatten()

        fun getLocations(urlRecord: UrlRecord, wordRecord: WordRecord) =
            wordsLocation
                .filter { it.urlRecord.equals(urlRecord) && it.wordRecord.equals(wordRecord) }
                .map { it.location }


        val urls = wordsLocation.map { it.urlRecord }.distinct()


        data class LocRowWithMetrics(val locationRow: WordsLocationRow, val m1: Double, val m2: Double, val m3: Double)

        val locationRows = urls.map mapUrl@{ url ->
            val locations = words.map { word ->
                getLocations(url, word).ifEmpty { return@mapUrl emptyList() }
            }
            product(locations).map { p -> WordsLocationRow(url, p) }
        }.flatten()

        println(locationRows)


        val min = pageRankingService.getMinLocationScore(locationRows)

        val result = locationRows
            .map {
                val m1 = pageRankingService.getPageRank(it.url)
                val m2 = pageRankingService.getNormalizedLocationScore(it, min)
                val m3 = (m1 + m2) / 2
                LocRowWithMetrics(it, m1, m2, m3)
            }
            .sortedByDescending { it.m3 }
            .distinctBy { it.locationRow.url }
            .take(20)


        return SearchResult(
            words.map { Pair(it.id!!, it.word) },
            result.map { PageRankingRow(it.locationRow.url.id!!, it.m1, it.m2, it.m3, it.locationRow.url.url) },
            WordsLocationTable(words.map { it.word }, result.map { it.locationRow }),
            locationRows.map { it.url }.distinct().size,
            getHtmlMarkedContents(result.take(3).map { it.locationRow.url }, words)
        )
    }

    private fun getHtmlMarkedContents(urls: List<UrlRecord>, words: List<WordRecord>): List<String> {

        val availableColors = listOf(0xFFFF00, 0x00FF00, 0xC0C0C0, 0x00FFFF, 0xFFD700, 0xFF7F50, 0x87CEEB, 0xFFC0CB)
        var idx = 0
        fun getColor() = "#%06X".format(availableColors[idx++.mod(availableColors.size)].and(0xFFFFFF))

        val wordToColor = mutableMapOf<String, String>()

        return urls.map { url ->
            wordLocationRepository.findAllByUrlRecordOrderByLocationAsc(url)
                .take(1024)
                .map { it.wordRecord }
                .map {
                    if (words.any { w -> w.word.equals(it.word, true) }) {
                        "<span style='background-color: ${wordToColor.computeIfAbsent(it.word) {getColor()}};'>${it.word}</span>"
                    } else it.word
                }
                .joinToString(separator = " ") { it }
        }
    }
}
