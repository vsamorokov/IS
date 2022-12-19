package me.vsamorokov.crawler.time;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Component
public class TimeCounter {

    private final LongAdder timeAdder = new LongAdder();
    private final AtomicInteger count = new AtomicInteger();
    private final AtomicLong max = new AtomicLong(Long.MIN_VALUE);
    private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);

    public void count(long time) {
        count.incrementAndGet();
        timeAdder.add(time);

        long _max = max.get();
        if(_max < time){
            max.compareAndSet(_max, time);
        }

        long _min = min.get();
        if(_min > time){
            min.compareAndSet(_min, time);
        }
    }

    public long getTotal() {
        return timeAdder.sum();
    }

    public double getAverage() {
        long sum = timeAdder.sum();
        int count = this.count.get();
        return 1.0 * sum / count;
    }

    public long getMax() {
        return max.get();
    }

    public long getMin() {
        return min.get();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("Total", getTotal());
        res.put("Average", getAverage());
        res.put("Max", getMax());
        res.put("Min", getMin());
        return res;
    }
}
