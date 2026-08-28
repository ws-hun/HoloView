package com.boilingpoint.news.converter;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.common.enums.HotTrend;
import com.boilingpoint.news.entity.HotHistoryEntity;
import com.boilingpoint.news.entity.HotItemEntity;
import com.boilingpoint.news.vo.HotDetailVO;
import com.boilingpoint.news.vo.HotItemVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotItemConverterTest {

    private final HotItemConverter converter = new HotItemConverter();

    @Test
    void shouldConvertEntityWithoutExposingInternalFields() {
        HotItemEntity entity = createHotItem();

        HotItemVO result = converter.toVO(entity);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.source()).isEqualTo(HotSource.WEIBO);
        assertThat(result.sourceName()).isEqualTo("微博");
        assertThat(result.categoryName()).isEqualTo("科技");
        assertThat(result.hotValue()).isEqualTo(9_850_000L);
    }

    @Test
    void shouldBuildDetailWithHistoryAndRelatedItems() {
        HotHistoryEntity history = HotHistoryEntity.builder()
                .hotId(10L)
                .hotValue(9_000_000L)
                .rank(2)
                .recordedAt(LocalDateTime.of(2026, 8, 27, 12, 0))
                .build();

        HotDetailVO result = converter.toDetailVO(createHotItem(), List.of(history), List.of(createHotItem()));

        assertThat(result.trendPoints()).hasSize(1);
        assertThat(result.trendPoints().get(0).rank()).isEqualTo(2);
        assertThat(result.relatedItems()).hasSize(1);
    }

    private HotItemEntity createHotItem() {
        return HotItemEntity.builder()
                .id(10L)
                .title("AI行业迎来重大进展")
                .description("人工智能行业最新进展")
                .source(HotSource.WEIBO)
                .sourceItemKey("weibo-10")
                .sourceUrl("https://example.com/hot/10")
                .category(HotCategory.TECHNOLOGY)
                .hotValue(9_850_000L)
                .hotValueText("985万")
                .rank(1)
                .previousRank(4)
                .rankChange(3)
                .trend(HotTrend.UP)
                .status(1)
                .deleted(0)
                .updatedAt(LocalDateTime.of(2026, 8, 27, 12, 5))
                .build();
    }
}
