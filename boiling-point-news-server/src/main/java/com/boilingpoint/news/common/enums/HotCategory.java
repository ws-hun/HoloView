package com.boilingpoint.news.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HotCategory {

    GENERAL("GENERAL", "综合"),
    SOCIETY("SOCIETY", "社会"),
    TECHNOLOGY("TECHNOLOGY", "科技"),
    ENTERTAINMENT("ENTERTAINMENT", "娱乐"),
    SPORTS("SPORTS", "体育"),
    FINANCE("FINANCE", "财经"),
    INTERNATIONAL("INTERNATIONAL", "国际"),
    GAMING("GAMING", "游戏"),
    AUTOMOTIVE("AUTOMOTIVE", "汽车"),
    LIFESTYLE("LIFESTYLE", "生活");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;
}
