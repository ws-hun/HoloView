package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class Jin10HotSearchCollectorTest {

    @Test
    void shouldParsePublicFlashPayloadAndDirectDetails() {
        Jin10HotSearchCollector collector = new Jin10HotSearchCollector(new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", 2, Duration.ofSeconds(1));
        var result = collector.parse("""
                var newest = [{"id":"202609010001","time":"2026-09-01 09:00:00","important":1,"channel":[1],"data":{"title":"【美股】市场开盘上涨","content":"科技股走强","pic":"https://img.example/a.jpg"}}, {"id":"202609000002","time":"2026-09-01 08:59:00","important":0,"channel":[5],"data":{"content":"【广告】不应入榜"}}];
                """);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.JIN10);
        assertThat(result.get(0).title()).isEqualTo("美股");
        assertThat(result.get(0).description()).isEqualTo("市场开盘上涨");
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://flash.jin10.com/detail/202609010001");
        assertThat(result.get(0).hotValueText()).isEqualTo("重要快讯");
    }

    @Test
    void shouldRejectEmptyOrInvalidPayload() {
        Jin10HotSearchCollector collector = new Jin10HotSearchCollector(new ObjectMapper(),
                new BaiduCategoryClassifier(), "https://example.com", 2, Duration.ofSeconds(1));

        assertThatIllegalStateException()
                .isThrownBy(() -> collector.parse(" "))
                .withMessage("Jin10 hot board data is empty");
        assertThatIllegalStateException()
                .isThrownBy(() -> collector.parse("var newest = {};"))
                .withMessage("Jin10 hot board list is missing");
    }
}
