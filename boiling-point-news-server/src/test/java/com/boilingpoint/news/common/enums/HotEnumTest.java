package com.boilingpoint.news.common.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HotEnumTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldExposeStableDatabaseCodes() {
        assertThat(HotSource.WEIBO.getCode()).isEqualTo("WEIBO");
        assertThat(HotCategory.TECHNOLOGY.getCode()).isEqualTo("TECHNOLOGY");
        assertThat(HotTrend.NEW.getCode()).isEqualTo("NEW");
    }

    @Test
    void shouldSerializeEnumAsCode() throws Exception {
        assertThat(objectMapper.writeValueAsString(HotSource.DOUYIN)).isEqualTo("\"DOUYIN\"");
        assertThat(objectMapper.writeValueAsString(HotTrend.UP)).isEqualTo("\"UP\"");
    }
}
