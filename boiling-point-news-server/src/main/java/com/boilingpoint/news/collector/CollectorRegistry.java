package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CollectorRegistry {

    private final Map<HotSource, HotSearchCollector> collectors;

    public CollectorRegistry(List<HotSearchCollector> collectorList) {
        this.collectors = new EnumMap<>(HotSource.class);
        for (HotSearchCollector collector : collectorList) {
            HotSearchCollector previous = collectors.put(collector.source(), collector);
            if (previous != null) {
                throw new IllegalStateException("Duplicate collector for source: " + collector.source());
            }
        }
        log.info("Hot collectors registered: sources={}", collectors.keySet());
    }

    public List<HotSearchCollector> all() {
        return List.copyOf(collectors.values());
    }
}
