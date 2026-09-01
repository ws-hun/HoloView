package com.boilingpoint.news.config;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.common.enums.HotTrend;
import com.boilingpoint.news.vo.HotItemVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RedisConfigTest {

    @Test
    void shouldRoundTripCachedHotItemsWithJavaTimeValues() {
        var serializer = new RedisConfig().valueSerializer();
        LocalDateTime publishedAt = LocalDateTime.of(2026, 9, 1, 10, 30);
        HotItemVO item = new HotItemVO(1L, "Public story", null, HotSource.HACKER_NEWS,
                "Hacker News", "https://news.ycombinator.com/item?id=1", HotCategory.TECHNOLOGY,
                "科技", 100L, "100 points", 1, null, 0, HotTrend.NEW, null,
                publishedAt, publishedAt.plusMinutes(1));

        byte[] serialized = serializer.serialize(List.of(item));
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(List.of(item));
    }
}
