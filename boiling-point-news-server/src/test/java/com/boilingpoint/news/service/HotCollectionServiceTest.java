package com.boilingpoint.news.service;

import com.boilingpoint.news.collector.CollectedHotItem;
import com.boilingpoint.news.collector.HotSearchCollector;
import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.common.enums.HotTrend;
import com.boilingpoint.news.entity.HotHistoryEntity;
import com.boilingpoint.news.entity.HotItemEntity;
import com.boilingpoint.news.mapper.HotHistoryMapper;
import com.boilingpoint.news.mapper.HotItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotCollectionServiceTest {

    @Mock
    private HotItemMapper hotItemMapper;
    @Mock
    private HotHistoryMapper hotHistoryMapper;
    @Mock
    private HotCacheService hotCacheService;
    @Mock
    private HotSearchCollector collector;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private HotCollectionService service;

    @BeforeEach
    void setUp() {
        service = new HotCollectionService(hotItemMapper, hotHistoryMapper, hotCacheService, eventPublisher);
        when(collector.source()).thenReturn(HotSource.WEIBO);
    }

    @Test
    void shouldInsertNewItemAndHistorySnapshot() {
        when(hotItemMapper.selectBySourceAndKey(HotSource.WEIBO, "item-1")).thenReturn(null);
        doAnswer(invocation -> {
            HotItemEntity entity = invocation.getArgument(0);
            entity.setId(901L);
            return 1;
        }).when(hotItemMapper).insert(any(HotItemEntity.class));

        int result = service.persist(collector, List.of(item("item-1", 3, 8_000L)));

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<HotItemEntity> itemCaptor = ArgumentCaptor.forClass(HotItemEntity.class);
        verify(hotItemMapper).insert(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getTrend()).isEqualTo(HotTrend.NEW);
        assertThat(itemCaptor.getValue().getRankChange()).isZero();
        ArgumentCaptor<HotHistoryEntity> historyCaptor = ArgumentCaptor.forClass(HotHistoryEntity.class);
        verify(hotHistoryMapper).insert(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getHotId()).isEqualTo(901L);
        assertThat(historyCaptor.getValue().getRank()).isEqualTo(3);
        verify(hotCacheService).evictByPrefix("hot:list:");
        verify(hotItemMapper).markMissingItemsInactive(HotSource.WEIBO, List.of("item-1"));
        verify(eventPublisher).publishEvent(any(com.boilingpoint.news.event.HotCollectionCompletedEvent.class));
    }

    @Test
    void shouldCalculateRankChangeWhenUpdatingExistingItem() {
        HotItemEntity existing = HotItemEntity.builder()
                .id(101L).source(HotSource.WEIBO).sourceItemKey("item-1")
                .rank(8).createdAt(java.time.LocalDateTime.now().minusDays(1)).build();
        when(hotItemMapper.selectBySourceAndKey(HotSource.WEIBO, "item-1")).thenReturn(existing);

        service.persist(collector, List.of(item("item-1", 3, 9_000L)));

        ArgumentCaptor<HotItemEntity> captor = ArgumentCaptor.forClass(HotItemEntity.class);
        verify(hotItemMapper).updateById(captor.capture());
        assertThat(captor.getValue().getPreviousRank()).isEqualTo(8);
        assertThat(captor.getValue().getRankChange()).isEqualTo(5);
        assertThat(captor.getValue().getTrend()).isEqualTo(HotTrend.UP);
        verify(hotHistoryMapper).insert(any(HotHistoryEntity.class));
    }

    @Test
    void shouldIgnoreEmptyCollectionWithoutEvictingCache() {
        assertThat(service.persist(collector, List.of())).isZero();
        org.mockito.Mockito.verifyNoInteractions(hotItemMapper, hotHistoryMapper, hotCacheService, eventPublisher);
    }

    private CollectedHotItem item(String key, int rank, long hotValue) {
        return new CollectedHotItem(key, "热点标题", "热点描述", HotSource.WEIBO,
                HotCategory.TECHNOLOGY, hotValue, String.valueOf(hotValue), rank,
                "https://example.com/" + key, null, null);
    }
}
