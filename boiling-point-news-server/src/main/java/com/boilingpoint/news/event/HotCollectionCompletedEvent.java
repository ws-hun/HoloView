package com.boilingpoint.news.event;

import com.boilingpoint.news.common.enums.HotSource;

import java.time.LocalDateTime;

public record HotCollectionCompletedEvent(
        HotSource source,
        int persistedCount,
        LocalDateTime completedAt
) {
}
