package com.boilingpoint.news.service;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.common.enums.HotTrend;
import com.boilingpoint.news.converter.HotItemConverter;
import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.dto.HotSearchQueryDTO;
import com.boilingpoint.news.entity.HotHistoryEntity;
import com.boilingpoint.news.entity.HotItemEntity;
import com.boilingpoint.news.exception.BusinessException;
import com.boilingpoint.news.mapper.HotHistoryMapper;
import com.boilingpoint.news.mapper.HotItemMapper;
import com.boilingpoint.news.service.impl.HotQueryServiceImpl;
import com.boilingpoint.news.vo.HotDetailVO;
import com.boilingpoint.news.vo.HotItemVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotQueryServiceImplTest {

    @Mock
    private HotItemMapper hotItemMapper;

    @Mock
    private HotHistoryMapper hotHistoryMapper;

    @Mock
    private HotCacheService hotCacheService;

    private HotQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new HotQueryServiceImpl(hotItemMapper, hotHistoryMapper, new HotItemConverter());
    }

    @Test
    void shouldNormalizeQueryWithoutMutatingCallerDto() {
        HotItemQueryDTO query = new HotItemQueryDTO();
        query.setKeyword("  人工智能  ");
        query.setLimit(500);
        when(hotItemMapper.selectHotItems(any())).thenReturn(List.of(createItem(101L, 1)));

        service.list(query);

        ArgumentCaptor<HotItemQueryDTO> captor = ArgumentCaptor.forClass(HotItemQueryDTO.class);
        verify(hotItemMapper).selectHotItems(captor.capture());
        assertThat(captor.getValue().getKeyword()).isEqualTo("人工智能");
        assertThat(captor.getValue().getLimit()).isEqualTo(100);
        assertThat(query.getKeyword()).isEqualTo("  人工智能  ");
        assertThat(query.getLimit()).isEqualTo(500);
    }

    @Test
    void shouldRejectBlankSearchKeyword() {
        HotSearchQueryDTO query = new HotSearchQueryDTO();
        query.setKeyword("  ");

        assertThatThrownBy(() -> service.search(query))
                .isInstanceOf(BusinessException.class)
                .hasMessage("搜索关键词不能为空");
    }

    @Test
    void shouldRejectInactiveDetail() {
        HotItemEntity item = createItem(101L, 1);
        item.setStatus(0);
        when(hotItemMapper.selectById(101L)).thenReturn(item);

        assertThatThrownBy(() -> service.getDetail(101L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("热点不存在或已下线");
    }

    @Test
    void shouldComposeDetailFromItemHistoryAndRelatedItems() {
        HotItemEntity item = createItem(101L, 1);
        HotItemEntity related = createItem(103L, 2);
        HotHistoryEntity history = HotHistoryEntity.builder()
                .hotId(101L)
                .hotValue(88_000_000L)
                .rank(2)
                .recordedAt(LocalDateTime.now().minusHours(1))
                .build();
        when(hotItemMapper.selectById(101L)).thenReturn(item);
        when(hotHistoryMapper.selectTrendPoints(eq(101L), any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(history));
        when(hotItemMapper.selectRelatedItems(HotCategory.TECHNOLOGY, 101L, 4))
                .thenReturn(List.of(related));

        HotDetailVO result = service.getDetail(101L);

        assertThat(result.item().id()).isEqualTo(101L);
        assertThat(result.trendPoints()).hasSize(1);
        assertThat(result.relatedItems()).extracting(itemVO -> itemVO.id()).containsExactly(103L);
    }

    @Test
    void shouldUseDedicatedLatestQuery() {
        when(hotItemMapper.selectLatestItems(5)).thenReturn(List.of(createItem(101L, 1)));

        assertThat(service.latest(5)).hasSize(1);

        verify(hotItemMapper).selectLatestItems(5);
    }

    @Test
    void shouldReturnListFromCacheWithoutQueryingMapper() {
        HotItemVO cached = new HotItemVO(101L, "缓存热点", "描述", HotSource.WEIBO, "微博", null,
                HotCategory.TECHNOLOGY, "科技", 1L, "1", 1, null, 0, HotTrend.STABLE,
                null, null, null);
        service = new HotQueryServiceImpl(hotItemMapper, hotHistoryMapper, new HotItemConverter(), hotCacheService);
        when(hotCacheService.get("hot:list:-:-:-::20")).thenReturn(List.of(cached));

        List<HotItemVO> result = service.list(new HotItemQueryDTO());

        assertThat(result).containsExactly(cached);
        org.mockito.Mockito.verifyNoInteractions(hotItemMapper);
    }

    private HotItemEntity createItem(Long id, int rank) {
        return HotItemEntity.builder()
                .id(id)
                .title("热点" + id)
                .source(HotSource.WEIBO)
                .sourceItemKey("weibo-" + id)
                .category(HotCategory.TECHNOLOGY)
                .hotValue(98_000_000L - rank)
                .hotValueText("9800万")
                .rank(rank)
                .rankChange(1)
                .trend(HotTrend.UP)
                .status(1)
                .deleted(0)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
