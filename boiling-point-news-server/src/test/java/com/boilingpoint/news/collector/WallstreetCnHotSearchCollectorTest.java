package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class WallstreetCnHotSearchCollectorTest {

    @Test
    void shouldParsePublicLiveFeedAndDirectDetails() {
        var collector = collector(2);
        var result = collector.parse("""
                {"code":20000,"data":{"items":[
                  {"id":3158233,"title":"人工智能安全风险受到关注","content_text":"监管部门发布最新说明","display_time":0,"uri":"https://wallstreetcn.com/livenews/3158233","cover_images":[{"uri":"https://img.example/cover.jpg"}]},
                  {"id":3158232,"title":"无效外链","uri":"https://search.example/query"}
                ]}}
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.WALLSTREET_CN);
        assertThat(result.get(0).sourceItemKey()).isEqualTo("wallstreet-cn-3158233");
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://wallstreetcn.com/livenews/3158233");
        assertThat(result.get(0).description()).isEqualTo("监管部门发布最新说明");
        assertThat(result.get(0).publishedAt()).isEqualTo(LocalDateTime.of(1970, 1, 1, 8, 0));
        assertThat(result.get(0).cover()).isEqualTo("https://img.example/cover.jpg");
    }

    @Test
    void shouldRejectInvalidOrEmptyFeed() {
        var collector = collector(2);

        assertThatIllegalStateException()
                .isThrownBy(() -> collector.parse("not-json"))
                .withMessage("WallstreetCN live feed data is invalid JSON");
        assertThatIllegalStateException()
                .isThrownBy(() -> collector.parse("{\"data\":{\"items\":[]}}"))
                .withMessage("WallstreetCN live feed contains no usable items");
    }

    private WallstreetCnHotSearchCollector collector(int limit) {
        return new WallstreetCnHotSearchCollector(new ObjectMapper(), new BaiduCategoryClassifier(),
                "https://example.com", limit, Duration.ofSeconds(1));
    }
}
