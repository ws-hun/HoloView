package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThat;

class HackerNewsHotSearchCollectorTest {

    @Test
    void shouldParseTopStoriesInBoardOrderAndUseDirectDetails() {
        HackerNewsHotSearchCollector collector = collector(2);
        var result = collector.parse("[123,456]", List.of(
                "{\"id\":456,\"type\":\"story\",\"title\":\"Second story\",\"score\":100}",
                "{\"id\":999,\"type\":\"story\",\"title\":\"Not on board\",\"score\":999}",
                "{\"id\":123,\"type\":\"story\",\"title\":\"Show HN: Public project\",\"score\":321,\"time\":0}"));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).source()).isEqualTo(HotSource.HACKER_NEWS);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://news.ycombinator.com/item?id=123");
        assertThat(result.get(0).hotValue()).isEqualTo(321L);
        assertThat(result.get(0).publishedAt()).isEqualTo(LocalDateTime.of(1970, 1, 1, 8, 0));
        assertThat(result.get(1).sourceUrl()).isEqualTo("https://news.ycombinator.com/item?id=456");
    }

    @Test
    void shouldIgnoreMalformedAndNonStoryItems() {
        HackerNewsHotSearchCollector collector = collector(5);

        assertThatIllegalStateException().isThrownBy(() -> collector.parse("[1,2,3]", List.of(
                "not-json",
                "{\"id\":1,\"type\":\"comment\",\"title\":\"Comment\"}",
                "{\"id\":2,\"type\":\"story\",\"deleted\":true,\"title\":\"Deleted\"}",
                "{\"id\":3,\"type\":\"story\",\"title\":\"   \"}"
        ))).withMessage("Hacker News hot board contains no usable items");
    }

    @Test
    void shouldRejectInvalidTopStoriesPayload() {
        HackerNewsHotSearchCollector collector = collector(2);

        assertThatIllegalStateException()
                .isThrownBy(() -> collector.parse("{}", List.of()))
                .withMessage("Hacker News top stories list is missing");
    }

    private HackerNewsHotSearchCollector collector(int limit) {
        return new HackerNewsHotSearchCollector(new ObjectMapper(), new BaiduCategoryClassifier(),
                "https://example.com", "https://example.com/item/", limit, Duration.ofSeconds(1));
    }
}
