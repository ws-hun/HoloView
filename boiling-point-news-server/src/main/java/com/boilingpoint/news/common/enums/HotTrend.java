package com.boilingpoint.news.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HotTrend {

    UP("UP", "上升"),
    DOWN("DOWN", "下降"),
    NEW("NEW", "新上榜"),
    STABLE("STABLE", "稳定");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;
}
