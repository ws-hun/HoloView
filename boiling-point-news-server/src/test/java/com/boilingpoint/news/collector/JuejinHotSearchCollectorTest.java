package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

class JuejinHotSearchCollectorTest {
    @Test
    void shouldParseRankedArticlesWithDirectDetails() {
        JuejinHotSearchCollector collector = new JuejinHotSearchCollector(new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", 1, Duration.ofSeconds(1));
        var result = collector.parse("""
                {"data":[{"content":{"content_id":"123","title":"Web Components 为什么火不起来？","brief":"前端开发讨论"},"content_counter":{"hot_rank":882}}]}
                """);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.JUEJIN);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://juejin.cn/post/123");
        assertThat(result.get(0).hotValue()).isEqualTo(882L);
    }
}
