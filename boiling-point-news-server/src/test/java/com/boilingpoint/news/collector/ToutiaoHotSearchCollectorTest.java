package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToutiaoHotSearchCollectorTest {

    private final ToutiaoHotSearchCollector collector = new ToutiaoHotSearchCollector(
            new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", 30, Duration.ofSeconds(1));

    @Test
    void shouldParsePublicHotBoardItemsAndDetails() {
        String json = """
                {"data":[
                  {"ClusterIdStr":"1001","Title":"比尔·盖茨关注AI","QueryWord":"比尔·盖茨关注AI","Url":"https://www.toutiao.com/article/1001","HotValue":"52662955","InterestCategory":["technology"],"Image":{"url":"https://img.example.com/1.jpg"}},
                  {"ClusterIdStr":"1002","Title":"无效条目","Url":"sslocal://detail"}
                ]}
                """;

        List<CollectedHotItem> result = collector.parse(json);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(com.boilingpoint.news.common.enums.HotSource.TOUTIAO);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.TECHNOLOGY);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://www.toutiao.com/article/1001");
        assertThat(result.get(0).hotValue()).isEqualTo(52662955L);
    }

    @Test
    void shouldRejectPayloadWithoutUsableItems() {
        assertThatThrownBy(() -> collector.parse("{\"data\":[]}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Toutiao hot board contains no usable items");
    }
}
