package me.vsamorokov.searchengine

import me.vsamorokov.data.config.DataConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
@EnableCaching
@Import(DataConfiguration::class)
class Main

fun main() {
    runApplication<Main>()
}

@Configuration
class Config {

    @Bean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager("pageRanks")
    }
}
