package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;

import java.util.List;

public interface HotSearchCollector {

    HotSource source();

    List<CollectedHotItem> collect();
}
