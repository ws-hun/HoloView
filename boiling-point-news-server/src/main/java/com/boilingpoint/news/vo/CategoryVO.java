package com.boilingpoint.news.vo;

import com.boilingpoint.news.common.enums.HotCategory;

public record CategoryVO(
        Long id,
        String name,
        HotCategory code,
        Integer sort
) {
}
