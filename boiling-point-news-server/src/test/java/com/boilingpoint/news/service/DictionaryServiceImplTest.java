package com.boilingpoint.news.service;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.converter.DictionaryConverter;
import com.boilingpoint.news.entity.HotCategoryEntity;
import com.boilingpoint.news.entity.SourcePlatformEntity;
import com.boilingpoint.news.mapper.HotCategoryMapper;
import com.boilingpoint.news.mapper.SourcePlatformMapper;
import com.boilingpoint.news.service.impl.CategoryServiceImpl;
import com.boilingpoint.news.service.impl.PlatformServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceImplTest {

    @Mock
    private SourcePlatformMapper sourcePlatformMapper;

    @Mock
    private HotCategoryMapper hotCategoryMapper;

    @Mock
    private HotQueryService hotQueryService;

    @Test
    void shouldConvertEnabledPlatformDictionary() {
        SourcePlatformEntity platform = SourcePlatformEntity.builder()
                .id(1L).name("微博").code(HotSource.WEIBO).status(1).sort(10).build();
        when(sourcePlatformMapper.selectEnabledPlatforms()).thenReturn(List.of(platform));
        PlatformServiceImpl service = new PlatformServiceImpl(
                sourcePlatformMapper, new DictionaryConverter(), hotQueryService);

        assertThat(service.listEnabledPlatforms()).singleElement()
                .satisfies(item -> assertThat(item.code()).isEqualTo(HotSource.WEIBO));
    }

    @Test
    void shouldDelegateCategoryHotQueryWithCategoryAndLimit() {
        CategoryServiceImpl service = new CategoryServiceImpl(
                hotCategoryMapper, new DictionaryConverter(), hotQueryService);

        service.getCategoryHotItems(HotCategory.SOCIETY, 12);

        ArgumentCaptor<com.boilingpoint.news.dto.HotItemQueryDTO> captor =
                ArgumentCaptor.forClass(com.boilingpoint.news.dto.HotItemQueryDTO.class);
        verify(hotQueryService).list(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo(HotCategory.SOCIETY);
        assertThat(captor.getValue().getLimit()).isEqualTo(12);
    }

    @Test
    void shouldConvertEnabledCategoryDictionary() {
        HotCategoryEntity category = HotCategoryEntity.builder()
                .id(1L).name("科技").code(HotCategory.TECHNOLOGY).status(1).sort(20).build();
        when(hotCategoryMapper.selectEnabledCategories()).thenReturn(List.of(category));
        CategoryServiceImpl service = new CategoryServiceImpl(
                hotCategoryMapper, new DictionaryConverter(), hotQueryService);

        assertThat(service.listEnabledCategories()).singleElement()
                .satisfies(item -> assertThat(item.code()).isEqualTo(HotCategory.TECHNOLOGY));
    }
}
