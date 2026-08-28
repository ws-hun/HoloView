package com.boilingpoint.news.scheduler;

import com.boilingpoint.news.collector.CollectorRegistry;
import com.boilingpoint.news.collector.HotSearchCollector;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.service.HotCollectionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HotCollectionSchedulerTest {

    @Test
    void shouldContinueWhenOneCollectorFails() {
        HotSearchCollector failed = collector(HotSource.WEIBO);
        HotSearchCollector succeeded = collector(HotSource.ZHIHU);
        when(failed.collect()).thenThrow(new IllegalStateException("source unavailable"));
        when(succeeded.collect()).thenReturn(List.of());
        CollectorRegistry registry = new CollectorRegistry(List.of(failed, succeeded));
        HotCollectionService collectionService = mock(HotCollectionService.class);
        when(collectionService.persist(succeeded, List.of())).thenReturn(0);

        new HotCollectionScheduler(registry, collectionService).collect();

        verify(failed).collect();
        verify(succeeded).collect();
        verify(collectionService).persist(succeeded, List.of());
    }

    private HotSearchCollector collector(HotSource source) {
        HotSearchCollector collector = mock(HotSearchCollector.class);
        when(collector.source()).thenReturn(source);
        return collector;
    }
}
