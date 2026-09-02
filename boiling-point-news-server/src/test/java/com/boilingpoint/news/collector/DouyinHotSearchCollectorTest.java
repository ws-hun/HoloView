package com.boilingpoint.news.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DouyinHotSearchCollectorTest {

    @Test
    void parsesPublicHotSearchPayload() {
        DouyinHotSearchCollector collector = new DouyinHotSearchCollector(
                new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", "", 10, Duration.ofSeconds(1));
        var result = collector.parse("""
                {"data":{"word_list":[{"sentence_id":"123","word":"测试热点","hot_value":456700}]}}
                """);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).sourceItemKey()).isEqualTo("douyin-123");
        assertThat(result.get(0).hotValue()).isEqualTo(456700L);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://www.douyin.com/hot/123");
    }

    @Test
    void parsesFallbackItems() {
        DouyinHotSearchCollector collector = new DouyinHotSearchCollector(
                new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", "", 10, Duration.ofSeconds(1));
        var result = collector.parse("""
                {"items":[{"id":"abc","title":"备用热点","hotValue":1200,"url":"https://www.douyin.com/hot/abc"}]}
                """);
        assertThat(result.get(0).title()).isEqualTo("备用热点");
        assertThat(result.get(0).hotValue()).isEqualTo(1200L);
    }

    @Test
    void parsesStringHotValueWithUnit() {
        DouyinHotSearchCollector collector = new DouyinHotSearchCollector(
                new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", "", 10, Duration.ofSeconds(1));
        var result = collector.parse("""
                {"data":{"word_list":[{"sentence_id":"unit","word":"单位热点","hot_value":"12.5万"}]}}
                """);
        assertThat(result.get(0).hotValue()).isEqualTo(125_000L);
    }
}
