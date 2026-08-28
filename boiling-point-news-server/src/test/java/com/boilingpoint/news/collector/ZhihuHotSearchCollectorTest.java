package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZhihuHotSearchCollectorTest {

    private ZhihuHotSearchCollector collector;

    @BeforeEach
    void setUp() {
        collector = new ZhihuHotSearchCollector(new ObjectMapper(), new BaiduCategoryClassifier(),
                "https://www.zhihu.com/api/v3/feed/topstory/hot-list-web?limit=20&desktop=true",
                2, Duration.ofSeconds(1));
    }

    @Test
    void shouldParsePublicHotBoardItemsAndQuestionDetails() {
        String json = """
                {"data":[
                  {"target":{"title_area":{"text":"国产大模型如何影响普通人"},"excerpt_area":{"text":"人工智能应用正在加速"},"metrics_area":{"text":"1.2 万热度"},"image_area":{"url":"https://img.example.com/1.jpg"},"link":{"url":"https://www.zhihu.com/question/195000001"}}},
                  {"target":{"title_area":{"text":"新能源汽车发展趋势"},"excerpt_area":{"text":"车企公布最新数据"},"metrics_area":{"text":"358.5 万热度"},"image_area":{"url":"https://img.example.com/2.jpg"},"link":{"url":"https://www.zhihu.com/question/195000002/answer/42"}}},
                  {"target":{"title_area":{"text":"超出限制"},"link":{"url":"https://www.zhihu.com/question/195000003"}}}
                ]}
                """;

        List<CollectedHotItem> result = collector.parse(json);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).source()).isEqualTo(HotSource.ZHIHU);
        assertThat(result.get(0).sourceItemKey()).isEqualTo("zhihu-195000001");
        assertThat(result.get(0).title()).isEqualTo("国产大模型如何影响普通人");
        assertThat(result.get(0).category()).isEqualTo(HotCategory.TECHNOLOGY);
        assertThat(result.get(0).hotValue()).isEqualTo(12_000L);
        assertThat(result.get(0).hotValueText()).isEqualTo("1.2 万热度");
        assertThat(result.get(0).cover()).isEqualTo("https://img.example.com/1.jpg");
        assertThat(result.get(1).sourceItemKey()).isEqualTo("zhihu-195000002");
        assertThat(result.get(1).category()).isEqualTo(HotCategory.AUTOMOTIVE);
        assertThat(result.get(1).hotValue()).isEqualTo(3_585_000L);
        assertThat(result.get(1).rank()).isEqualTo(2);
    }

    @Test
    void shouldConvertChineseMetricUnits() {
        assertThat(collector.parseHotValue("987 浏览")).isEqualTo(987L);
        assertThat(collector.parseHotValue("2.5万 热度")).isEqualTo(25_000L);
        assertThat(collector.parseHotValue("1.08 亿热度")).isEqualTo(108_000_000L);
        assertThat(collector.parseHotValue("暂无热度")).isZero();
    }

    @Test
    void shouldSkipInvalidLinksAndRejectEmptyBoards() {
        String json = """
                {"data":[
                  {"target":{"title_area":{"text":"脚本链接"},"link":{"url":"javascript:alert(1)"}}},
                  {"target":{"title_area":{"text":"相对链接"},"link":{"url":"/question/123"}}}
                ]}
                """;

        assertThatThrownBy(() -> collector.parse(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Zhihu hot board contains no usable items");
    }

    @Test
    void shouldFailClearlyWhenPayloadChanges() {
        assertThatThrownBy(() -> collector.parse("not-json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Zhihu hot board data is invalid JSON");
        assertThatThrownBy(() -> collector.parse("{\"data\":{}}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Zhihu hot board list is missing");
    }
}
