package me.vsamorokov.searchengine.bean

import me.vsamorokov.data.entity.UrlRecord

data class SearchResult(val words: List<Pair<Long, String>>, val rankings: List<PageRankingRow>, val table: WordsLocationTable, val matchedUrls: Int, val contents: List<String>,)

data class PageRankingRow(val urlId: Long, val m1: Double, val m2: Double, val m3: Double, val url: String,)

data class WordsLocationTable(val words: List<String>, val rows: List<WordsLocationRow>)

data class WordsLocationRow(val url: UrlRecord, val locations: List<Int>)
