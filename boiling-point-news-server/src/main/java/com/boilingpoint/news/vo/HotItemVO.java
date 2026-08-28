package com.boilingpoint.news.vo;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.common.enums.HotTrend;

import java.time.LocalDateTime;

public record HotItemVO(
        Long id,
        String title,
        String description,
        HotSource source,
        String sourceName,
        String sourceUrl,
        HotCategory category,
        String categoryName,
        Long hotValue,
        String hotValueText,
        Integer rank,
        Integer previousRank,
        Integer rankChange,
        HotTrend trend,
        String cover,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
