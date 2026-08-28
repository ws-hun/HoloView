package com.boilingpoint.news.vo;

import com.boilingpoint.news.common.enums.HotSource;

public record PlatformVO(
        Long id,
        String name,
        HotSource code,
        String logo,
        Integer sort
) {
}
