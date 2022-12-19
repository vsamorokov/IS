package me.vsamorokov.crawler;

import lombok.extern.slf4j.Slf4j;
import me.vsamorokov.crawler.metrics.MetricsCounter;
import me.vsamorokov.crawler.time.TimeCounter;
import me.vsamorokov.data.config.DataConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

@Slf4j
@Import(DataConfiguration.class)
@SpringBootApplication
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(Crawler crawler, TimeCounter timeCounter, MetricsCounter metricsCounter, @Value("${crawler.urls}") List<String> urls) {
        return args -> {
            crawler.crawl(urls);
            crawler.waitToComplete();
            crawler.shutdown();
            log.info("Crawler finished. Time stats (ms): {}", timeCounter.getStats());
            log.info("Metrics:\n{}", metricsCounter.getMetrics());
        };
    }
}
