package com.boilingpoint.news.service;

import com.boilingpoint.news.collector.CollectedHotItem;
import com.boilingpoint.news.collector.HotSearchCollector;
import com.boilingpoint.news.common.enums.HotTrend;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.entity.HotHistoryEntity;
import com.boilingpoint.news.entity.HotItemEntity;
import com.boilingpoint.news.mapper.HotHistoryMapper;
import com.boilingpoint.news.mapper.HotItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotCollectionService {

    private final HotItemMapper hotItemMapper;
    private final HotHistoryMapper hotHistoryMapper;
    private final HotCacheService hotCacheService;

    @Transactional
    public int persist(HotSearchCollector collector, List<CollectedHotItem> items) {
        if (items == null || items.isEmpty()) {
            log.warn("Collector returned no hot items: source={}", collector.source());
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        int persisted = 0;
        for (CollectedHotItem collected : items) {
            if (collected == null || collected.sourceItemKey() == null || collected.sourceItemKey().isBlank()
                    || collected.title() == null || collected.title().isBlank()) {
                log.warn("Skipping invalid collected hot item: source={}", collector.source());
                continue;
            }
            HotItemEntity existing = hotItemMapper.selectBySourceAndKey(collector.source(), collected.sourceItemKey());
            HotItemEntity entity = toEntity(collector.source(), collected, existing, now);
            if (existing == null) {
                hotItemMapper.insert(entity);
            } else {
                hotItemMapper.updateById(entity);
            }
            hotHistoryMapper.insert(HotHistoryEntity.builder()
                    .hotId(entity.getId())
                    .hotValue(entity.getHotValue())
                    .rank(entity.getRank())
                    .recordedAt(now)
                    .build());
            persisted++;
        }
        hotCacheService.evictByPrefix("hot:list:");
        hotCacheService.evictByPrefix("hot:latest:");
        hotCacheService.evictByPrefix("hot:detail:");
        hotCacheService.evictByPrefix("hot:trend:");
        log.info("Hot collection persisted: source={}, collectedCount={}, persistedCount={}",
                collector.source(), items.size(), persisted);
        return persisted;
    }

    private HotItemEntity toEntity(HotSource source, CollectedHotItem collected,
                                   HotItemEntity existing, LocalDateTime now) {
        Integer previousRank = existing == null ? null : existing.getRank();
        Integer rankChange = existing == null || previousRank == null || collected.rank() == null
                ? 0 : previousRank - collected.rank();
        HotTrend trend = existing == null ? HotTrend.NEW
                : rankChange > 0 ? HotTrend.UP
                : rankChange < 0 ? HotTrend.DOWN : HotTrend.STABLE;
        return HotItemEntity.builder()
                .id(existing == null ? null : existing.getId())
                .title(collected.title())
                .description(collected.description())
                .source(source)
                .sourceItemKey(collected.sourceItemKey())
                .sourceUrl(collected.sourceUrl())
                .category(collected.category() == null ? com.boilingpoint.news.common.enums.HotCategory.GENERAL
                        : collected.category())
                .hotValue(collected.hotValue() == null ? 0L : collected.hotValue())
                .hotValueText(collected.hotValueText() == null ? "0" : collected.hotValueText())
                .rank(collected.rank() == null ? 0 : collected.rank())
                .previousRank(previousRank)
                .rankChange(rankChange)
                .trend(trend)
                .cover(collected.cover())
                .publishedAt(collected.publishedAt())
                .status(1)
                .createdAt(existing == null ? now : existing.getCreatedAt())
                .updatedAt(now)
                .deleted(0)
                .build();
    }
}
