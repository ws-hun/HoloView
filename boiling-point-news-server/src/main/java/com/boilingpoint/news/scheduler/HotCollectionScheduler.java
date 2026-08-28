package com.boilingpoint.news.scheduler;

import com.boilingpoint.news.collector.CollectorRegistry;
import com.boilingpoint.news.collector.HotSearchCollector;
import com.boilingpoint.news.service.HotCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotCollectionScheduler {

    private final CollectorRegistry collectorRegistry;
    private final HotCollectionService hotCollectionService;

    @Scheduled(initialDelayString = "${app.collector.initial-delay:60000}",
            fixedDelayString = "${app.collector.fixed-delay:60000}")
    public void collect() {
        long startedAt = System.nanoTime();
        int total = 0;
        for (HotSearchCollector collector : collectorRegistry.all()) {
            try {
                total += hotCollectionService.persist(collector, collector.collect());
            } catch (RuntimeException exception) {
                log.error("Hot collection failed: source={}", collector.source(), exception);
            }
        }
        log.info("Hot collection batch completed: collectorCount={}, persistedCount={}, durationMs={}",
                collectorRegistry.all().size(), total, (System.nanoTime() - startedAt) / 1_000_000);
    }
}
