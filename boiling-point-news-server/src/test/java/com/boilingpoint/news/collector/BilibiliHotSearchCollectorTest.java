package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

class BilibiliHotSearchCollectorTest {
    @Test
    void shouldParsePopularVideosWithDirectDetails() {
        BilibiliHotSearchCollector collector = new BilibiliHotSearchCollector(new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", 2, Duration.ofSeconds(1));
        var result = collector.parse("""
                {"data":{"list":[{"bvid":"BV1abc","title":"AI 新视频","desc":"人工智能趋势","pic":"https://i.example/a.jpg","owner":{"name":"UP主"},"stat":{"view":123456}}, {"bvid":"BV2def","title":"第二条","stat":{"view":42}}]}}
                """);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).source()).isEqualTo(HotSource.BILIBILI);
        assertThat(result.get(0).sourceItemKey()).isEqualTo("bilibili-BV1abc");
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://www.bilibili.com/video/BV1abc");
        assertThat(result.get(0).hotValue()).isEqualTo(123456L);
    }
}
