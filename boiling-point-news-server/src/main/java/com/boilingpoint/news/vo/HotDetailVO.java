package com.boilingpoint.news.vo;

import java.util.List;

public record HotDetailVO(
        HotItemVO item,
        List<HotTrendPointVO> trendPoints,
        List<HotItemVO> relatedItems
) {
}
