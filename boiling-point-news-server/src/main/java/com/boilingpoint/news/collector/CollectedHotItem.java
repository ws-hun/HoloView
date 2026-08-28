package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;

import java.time.LocalDateTime;

public record CollectedHotItem(
        String sourceItemKey,
        String title,
        String description,
        HotSource source,
        HotCategory category,
        Long hotValue,
        String hotValueText,
        Integer rank,
        String sourceUrl,
        String cover,
        LocalDateTime publishedAt
) {
}
