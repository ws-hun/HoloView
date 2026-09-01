package com.boilingpoint.news.mapper;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.common.enums.HotTrend;
import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.entity.HotHistoryEntity;
import com.boilingpoint.news.entity.HotItemEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class MapperIntegrationTest {

    @Autowired
    private HotItemMapper hotItemMapper;

    @Autowired
    private HotHistoryMapper hotHistoryMapper;

    @Autowired
    private SourcePlatformMapper sourcePlatformMapper;

    @Autowired
    private HotCategoryMapper hotCategoryMapper;

    @Test
    void shouldQueryRankingWithFiltersAndExcludeInactiveItems() {
        HotItemQueryDTO query = new HotItemQueryDTO();
        query.setCategory(HotCategory.TECHNOLOGY);
        query.setTrend(HotTrend.UP);
        query.setKeyword("经济");
        query.setLimit(10);

        List<HotItemEntity> result = hotItemMapper.selectHotItems(query);

        assertThat(result).extracting(HotItemEntity::getId).containsExactly(103L);
        assertThat(result.get(0).getSource()).isEqualTo(HotSource.ZHIHU);
    }

    @Test
    void shouldOrderPlatformItemsBySourceRankInsteadOfHotValue() {
        HotItemEntity first = createPlatformItem("rank-first", 1, 0L);
        HotItemEntity second = createPlatformItem("rank-second", 2, 10_000L);
        hotItemMapper.insert(first);
        hotItemMapper.insert(second);
        HotItemQueryDTO query = new HotItemQueryDTO();
        query.setSource(HotSource.JIN10);
        query.setLimit(10);

        List<HotItemEntity> result = hotItemMapper.selectHotItems(query);

        assertThat(result).extracting(HotItemEntity::getRank).containsExactly(1, 2);
    }

    @Test
    void shouldUseEscapedRankColumnForGeneratedCrud() {
        HotItemEntity item = HotItemEntity.builder()
                .title("Mapper新增热点")
                .source(HotSource.BAIDU)
                .sourceItemKey("baidu-new")
                .category(HotCategory.TECHNOLOGY)
                .hotValue(12_000_000L)
                .hotValueText("1200万")
                .rank(7)
                .rankChange(0)
                .trend(HotTrend.NEW)
                .status(1)
                .updatedAt(LocalDateTime.of(2026, 8, 27, 17, 20))
                .deleted(0)
                .build();

        assertThat(hotItemMapper.insert(item)).isEqualTo(1);
        assertThat(hotItemMapper.selectById(item.getId()).getRank()).isEqualTo(7);
    }

    @Test
    void shouldReturnRecentTrendPointsInChronologicalOrder() {
        List<HotHistoryEntity> result = hotHistoryMapper.selectTrendPoints(
                101L,
                LocalDateTime.of(2026, 8, 27, 9, 0),
                3
        );

        assertThat(result).extracting(HotHistoryEntity::getHotValue)
                .containsExactly(42_000_000L, 56_000_000L, 78_000_000L);
        assertThat(result).extracting(HotHistoryEntity::getRank)
                .containsExactly(6, 4, 2);
    }

    @Test
    void shouldReturnLatestItemsByUpdateTime() {
        List<HotItemEntity> result = hotItemMapper.selectLatestItems(2);

        assertThat(result).extracting(HotItemEntity::getId)
                .containsExactly(101L, 102L);
    }

    @Test
    void shouldReturnOnlyEnabledDictionariesInConfiguredOrder() {
        assertThat(sourcePlatformMapper.selectEnabledPlatforms())
                .extracting(platform -> platform.getCode().getCode())
                .containsExactly("WEIBO", "ZHIHU");
        assertThat(hotCategoryMapper.selectEnabledCategories())
                .extracting(category -> category.getCode().getCode())
                .containsExactly("GENERAL", "TECHNOLOGY");
    }

    @Test
    void shouldMarkItemsMissingFromLatestCollectionInactive() {
        int updated = hotItemMapper.markMissingItemsInactive(HotSource.WEIBO, List.of("not-present"));

        assertThat(updated).isEqualTo(1);
        assertThat(hotItemMapper.selectById(101L).getStatus()).isZero();
    }

    private HotItemEntity createPlatformItem(String key, int rank, long hotValue) {
        return HotItemEntity.builder()
                .title(key)
                .source(HotSource.JIN10)
                .sourceItemKey(key)
                .category(HotCategory.FINANCE)
                .hotValue(hotValue)
                .hotValueText("快讯")
                .rank(rank)
                .rankChange(0)
                .trend(HotTrend.NEW)
                .status(1)
                .updatedAt(LocalDateTime.of(2026, 9, 1, 11, 0))
                .deleted(0)
                .build();
    }
}
