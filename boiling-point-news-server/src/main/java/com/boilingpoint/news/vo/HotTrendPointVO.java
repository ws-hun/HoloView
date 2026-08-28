package com.boilingpoint.news.vo;

import java.time.LocalDateTime;

public record HotTrendPointVO(
        Long hotValue,
        Integer rank,
        LocalDateTime recordedAt
) {
}
