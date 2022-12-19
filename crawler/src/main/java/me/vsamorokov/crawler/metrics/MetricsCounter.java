package me.vsamorokov.crawler.metrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.vsamorokov.data.repository.UrlRecordRepository;
import me.vsamorokov.data.repository.WordRecordRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class MetricsCounter {

    private final UrlRecordRepository urlRecordRepository;
    private final WordRecordRepository wordRecordRepository;

    private final AtomicLong counter = new AtomicLong();
    private final List<Pair<Long, Long>> data = new ArrayList<>();

    @Value("${crawler.metrics.period:50}")
    private int period;

    public synchronized void count() {
        if (counter.incrementAndGet() % period != 0) {
            return;
        }

        long urls = urlRecordRepository.count();
        long words = wordRecordRepository.count();
        data.add(Pair.of(urls, words));
    }

    public String getMetrics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Urls").append(" ").append("Words");
        for (Pair<Long, Long> p : data) {
            sb.append(p.getLeft()).append(" ").append(p.getRight());
        }
        return sb.toString();
    }
}
