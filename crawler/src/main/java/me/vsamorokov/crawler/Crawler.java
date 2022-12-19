package me.vsamorokov.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vsamorokov.crawler.metrics.MetricsCounter;
import me.vsamorokov.crawler.time.TimeCounter;
import me.vsamorokov.data.entity.*;
import me.vsamorokov.data.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static me.vsamorokov.crawler.QueryUtils.getOrCreate;

@Slf4j
@Service
@RequiredArgsConstructor
public class Crawler {

    private final TimeCounter timeCounter;
    private final MetricsCounter metricsCounter;
    private final WordRecordRepository wordRecordRepository;
    private final UrlRecordRepository urlRecordRepository;
    private final WordLocationRepository wordLocationRepository;
    private final LinkBetweenUrlRepository linkBetweenUrlRepository;
    private final LinkWordRepository linkWordRepository;

    private final ExecutorService executorService = Executors.newFixedThreadPool(16);
    private final AtomicInteger tasksRunning = new AtomicInteger(0);
    private final Set<String> urlsInProgress = ConcurrentHashMap.newKeySet();

    @Value("${crawler.max-depth}")
    private int maxDepth;

    @Value("${crawler.max-url-length:512}")
    private int maxUrlLength;

    @Value("${crawler.names-to-exclude:}")
    private Set<String> namesToExclude;

    public void waitToComplete() throws InterruptedException {
        Thread.sleep(2000L);
        while (true) {
            int tasks = tasksRunning.get();
            log.info("{} ACTIVE TASKS", tasks);
            if (tasks == 0) {
                return;
            }
            Thread.sleep(2000L);
        }
    }

    public void crawl(List<String> urls) {
        for (String url : urls) {
            runTask(new CrawlTask(url));
        }
    }

    private void runTask(CrawlTask task) {
        if (taskNotValid(task)) {
            return;
        }
        tasksRunning.incrementAndGet();
        try {
            executorService.submit(() -> {
                try {
                    if (!urlsInProgress.add(task.url)) {
                        return;
                    }
                    long start = System.currentTimeMillis();
                    try {
                        crawl(task);
                    } finally {
                        timeCounter.count(System.currentTimeMillis() - start);
                        urlsInProgress.remove(task.url);
                    }
                } catch (Exception e) {
                    log.error("Error occurred while crawling url {}", task.url, e);
                } finally {
                    tasksRunning.decrementAndGet();
                }
            });
        } catch (Exception e) {
            tasksRunning.decrementAndGet();
        }
    }

    private boolean taskNotValid(CrawlTask task) {
        if (task.currentDepth() > maxDepth) {
            return true;
        }
        if(task.url.length() >= maxUrlLength) {
            return true;
        }
        if (!StringUtils.startsWith(task.url, "http")) {
            return true;
        }
        return false;
    }

    private void crawl(CrawlTask task) throws Exception {

        UrlRecord urlRecord = urlRecordRepository.findByUrl(task.url);
        if (urlRecord != null && urlRecord.isCrawled()) {
            log.info("Url {} already crawled", task.url);
            return;
        }

        log.info("Starting task {}", task);

        Document document = Jsoup.connect(task.url).get();

        if (urlRecord == null) {
            urlRecord = urlRecordRepository.save(new UrlRecord(task.url, false, 1D));
        }

        Element body = document.body();
        String text = body.text();
        List<String> words = getWords(text);
        if (words.size() == 0) {
            log.info("No words detected at url {}. Skipping", task.url);
        }

        int index = 0;

        for (String word : words) {

            WordRecord wordRecord = getOrCreate(() -> wordRecordRepository.findByWord(word), () -> new WordRecord(word, shouldBeFiltered(word)), wordRecordRepository::save);

            int location = index++;

            UrlRecord finalUrlRecord = urlRecord;
            getOrCreate(() -> wordLocationRepository.findByWordRecordAndUrlRecordAndLocation(wordRecord, finalUrlRecord, location),
                    () -> new WordLocation(wordRecord, finalUrlRecord, location),
                    wordLocationRepository::save);
        }

        record LinkText(String url, String text) {
        }

        List<LinkText> links = body.getElementsByTag("a").stream()
                .map(l -> new LinkText(l.attributes().get("href"), l.text()))
                .filter(l -> StringUtils.startsWith(l.url, "http"))
                .toList();

        for (LinkText link : links) {

            String url = link.url;

            UrlRecord otherUrlRecord = getOrCreate(() -> urlRecordRepository.findByUrl(url), () -> new UrlRecord(url, false, 1D), urlRecordRepository::save);

            UrlRecord finalUrlRecord = urlRecord;
            LinkBetweenUrl linkBetweenUrl = getOrCreate(() -> linkBetweenUrlRepository.findByFromUrlAndToUrl(finalUrlRecord, otherUrlRecord),
                    () -> new LinkBetweenUrl(finalUrlRecord, otherUrlRecord),
                    linkBetweenUrlRepository::save);

            String linkText = link.text;
            if (!StringUtils.isNotEmpty(linkText)) {
                continue;
            }
            List<String> linkWords = getWords(linkText);
            for (String linkWord : linkWords) {

                WordRecord linkWordRecord = getOrCreate(() -> wordRecordRepository.findByWord(linkWord), () -> new WordRecord(linkWord, shouldBeFiltered(linkWord)), wordRecordRepository::save);

                getOrCreate(() -> linkWordRepository.findByWordRecordAndLinkBetweenUrl(linkWordRecord, linkBetweenUrl),
                        () -> new LinkWord(linkWordRecord, linkBetweenUrl),
                        linkWordRepository::save);
            }
        }

        urlRecord.setCrawled(true);
        urlRecordRepository.save(urlRecord);
        metricsCounter.count();

        links.stream()
                .map(link -> new CrawlTask(link.url, task.currentDepth() + 1))
                .forEach(this::runTask);
    }

    private List<String> getWords(String text) {
        return Arrays.stream(text.split(" "))
                .map(this::prepareWord)
                .filter(StringUtils::isNotBlank)
                .filter(w -> !namesToExclude.contains(w))
                .toList();
    }

    private String prepareWord(String s) {
        return s
                .replaceAll("[^а-яА-Яa-zA-Z\\d]", "")
                .toLowerCase();
    }

    private boolean shouldBeFiltered(String word) {
        return false;
    }


    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private record CrawlTask(String url, int currentDepth) {
        public CrawlTask(String url) {
            this(url, 0);
        }
    }
}
