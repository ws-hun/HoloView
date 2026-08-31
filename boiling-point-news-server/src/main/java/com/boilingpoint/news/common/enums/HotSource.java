package com.boilingpoint.news.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HotSource {

    WEIBO("WEIBO", "微博"),
    ZHIHU("ZHIHU", "知乎"),
    BAIDU("BAIDU", "百度"),
    DOUYIN("DOUYIN", "抖音"),
    TOUTIAO("TOUTIAO", "今日头条"),
    BILIBILI("BILIBILI", "哔哩哔哩"),
    JUEJIN("JUEJIN", "掘金"),
    THE_PAPER("THE_PAPER", "澎湃新闻"),
    ITHOME("ITHOME", "IT之家"),
    KR36("KR36", "36氪"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String code;

    private final String label;
}
