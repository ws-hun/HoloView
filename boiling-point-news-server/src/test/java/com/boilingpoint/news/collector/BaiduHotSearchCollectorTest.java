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

class BaiduHotSearchCollectorTest {

    private BaiduHotSearchCollector collector;

    @BeforeEach
    void setUp() {
        collector = new BaiduHotSearchCollector(new ObjectMapper(), new BaiduCategoryClassifier(),
                "https://top.baidu.com/board?tab=realtime", 2, Duration.ofSeconds(1));
    }

    @Test
    void shouldParseStructuredPageDataAndLimitItems() {
        String html = """
                <html><body><div id="sanRoot"><!--s-data:{"data":{"cards":[
                  {"component":"other","content":[]},
                  {"component":"hotList","content":[
                    {"word":"国产大模型取得突破","desc":"人工智能产业应用提速","hotScore":"98560000","url":"https://www.baidu.com/s?wd=ai","img":"https://img.example/1.jpg"},
                    {"word":"新能源汽车交付创新高","desc":"车企公布最新数据","hotScore":"75180000","url":"https://www.baidu.com/s?wd=car","img":"https://img.example/2.jpg"},
                    {"word":"第三条不应返回","desc":"测试数量限制","hotScore":"1","url":"https://example.com/3","img":""}
                  ]}
                ]}}--><main></main></div></body></html>
                """;

        List<CollectedHotItem> result = collector.parse(html);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).source()).isEqualTo(HotSource.BAIDU);
        assertThat(result.get(0).sourceItemKey()).startsWith("baidu-").hasSize(38);
        assertThat(result.get(0).title()).isEqualTo("国产大模型取得突破");
        assertThat(result.get(0).hotValue()).isEqualTo(98_560_000L);
        assertThat(result.get(0).hotValueText()).isEqualTo("9856.0万");
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.TECHNOLOGY);
        assertThat(result.get(1).category()).isEqualTo(HotCategory.AUTOMOTIVE);
        assertThat(result.get(1).rank()).isEqualTo(2);
    }

    @Test
    void shouldFailClearlyWhenPageStructureChanges() {
        assertThatThrownBy(() -> collector.parse("<html><body><div id='sanRoot'></div></body></html>"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Baidu hot board page data is missing");
        assertThatThrownBy(() -> collector.parse("<html><body></body></html>"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Baidu hot board root element is missing");
    }

    @Test
    void shouldFailInsteadOfReturningEmptyBoard() {
        String html = "<div id='sanRoot'><!--s-data:{\"data\":{\"cards\":[{\"component\":\"hotList\",\"content\":[]}]}}--></div>";

        assertThatThrownBy(() -> collector.parse(html))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Baidu hot board contains no usable items");
    }
}
