package com.boilingpoint.news.controller;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.exception.GlobalExceptionHandler;
import com.boilingpoint.news.service.CategoryService;
import com.boilingpoint.news.service.PlatformService;
import com.boilingpoint.news.vo.CategoryVO;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.PlatformVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DictionaryControllerTest {

    @Mock
    private PlatformService platformService;
    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PlatformController(platformService),
                        new CategoryController(categoryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListEnabledDictionaries() throws Exception {
        when(platformService.listEnabledPlatforms()).thenReturn(List.of(new PlatformVO(1L, "微博", HotSource.WEIBO, null, 1)));
        when(categoryService.listEnabledCategories()).thenReturn(List.of(new CategoryVO(1L, "科技", HotCategory.TECHNOLOGY, 1)));

        mockMvc.perform(get("/api/platform/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get("/api/category/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldQueryHotItemsByPlatformAndCategory() throws Exception {
        HotItemVO item = new HotItemVO(1L, "title", "description", HotSource.WEIBO, "微博", "https://weibo.com",
                HotCategory.TECHNOLOGY, "科技", 100L, "100", 1, null, 0,
                com.boilingpoint.news.common.enums.HotTrend.STABLE, null,
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        when(platformService.getPlatformHotItems(HotSource.WEIBO, 5)).thenReturn(List.of(item));
        when(categoryService.getCategoryHotItems(HotCategory.TECHNOLOGY, 8)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/platform/WEIBO/hot").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/api/category/TECHNOLOGY/hot").param("limit", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformService).getPlatformHotItems(HotSource.WEIBO, 5);
        verify(categoryService).getCategoryHotItems(HotCategory.TECHNOLOGY, 8);
    }

    @Test
    void shouldRejectUnknownPlatform() throws Exception {
        mockMvc.perform(get("/api/platform/UNKNOWN/hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("source: 参数格式不正确"));
    }
}
