package me.vsamorokov.searchengine.service

import me.vsamorokov.data.entity.UrlRecord
import me.vsamorokov.data.repository.LinkBetweenUrlRepository
import me.vsamorokov.data.repository.UrlRecordRepository
import me.vsamorokov.searchengine.bean.WordsLocationRow
import org.jboss.logging.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.math.abs

@Service
class PageRankingService(
    private val linkBetweenUrlRepository: LinkBetweenUrlRepository,
    private val urlRecordRepository: UrlRecordRepository,
    @Value("\${page-rank.coef:0.85}") private val coef: Double,
    @Value("\${page-rank.enabled:false}") private val enabled: Boolean,
) {
    val log: Logger = Logger.getLogger(PageRankingService::class.java)

    @Scheduled(fixedDelay = 1000L)
    fun updatePageRanks() {
        if (!enabled) return
        var count = 0L
        val urls = urlRecordRepository.findAllByCrawledIsTrue()
        log.info("Iteration started. ${urls.size} records found to update")
        for (url in urls) {
            val pointingLinks = linkBetweenUrlRepository.findAllByToUrlAndFromUrlNot(url, url);
            val sum = pointingLinks.sumOf {
                if (it.fromUrl.equals(it.toUrl)) {
                    log.error("Same from and to urls for link with id ${it.id}")
                    .0
                } else getPart(it.fromUrl)
            }
            val result = (1 - coef) + coef * sum
            if (abs(url.pageRank - result) > 0.05) {
                url.pageRank = result
                urlRecordRepository.save(url)
                log.info("Updated Page rank for urlRecord with id ${url.id} to $result")
                count++
            }
        }
        log.info("Iteration finished. $count records updated")
    }

    private fun getPart(urlRecord: UrlRecord): Double {
        val outgoingUrls = withCache(urlRecord) { linkBetweenUrlRepository.countAllByFromUrl(urlRecord) }.let {
            if (it == 0L) {
                log.error("No outgoing urls for urlRecord with id ${urlRecord.id}. Using 1")
                1L
            } else it
        }
        return urlRecord.pageRank / outgoingUrls
    }

    @Cacheable("pageRanks")
    fun getPageRank(urlRecord: UrlRecord): Double =
        urlRecord.pageRank / urlRecordRepository.findMaxPageRank()

    fun getMinLocationScore(locationRows: List<WordsLocationRow>): Int =
        locationRows.minOf { it.locations.sum() }

    fun getNormalizedLocationScore(locationRow: WordsLocationRow, min: Int): Double =
        1.0 * min / locationRow.locations.sum()


    private val cache = mutableMapOf<Long, Long>()

    fun withCache(urlRecord: UrlRecord, f: (UrlRecord) -> Long): Long {
        return cache.computeIfAbsent(urlRecord.id!!) { f(urlRecord) }
    }

}