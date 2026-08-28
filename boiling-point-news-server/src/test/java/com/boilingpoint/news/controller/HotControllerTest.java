package com.boilingpoint.news.controller;

import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.dto.HotSearchQueryDTO;
import com.boilingpoint.news.dto.HotTrendQueryDTO;
import com.boilingpoint.news.exception.GlobalExceptionHandler;
import com.boilingpoint.news.service.HotQueryService;
import com.boilingpoint.news.vo.HotDetailVO;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.HotTrendPointVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HotControllerTest {

    @Mock
    private HotQueryService hotQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HotController(hotQueryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldQueryHotListWithBoundParameters() throws Exception {
        when(hotQueryService.list(argThat(query -> query.getSource() == HotSource.WEIBO
                && query.getLimit() == 10))).thenReturn(List.of(item()));

        mockMvc.perform(get("/api/hot/list").param("source", "WEIBO").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldReturnHotDetail() throws Exception {
        when(hotQueryService.getDetail(12L)).thenReturn(new HotDetailVO(item(), List.of(), List.of()));

        mockMvc.perform(get("/api/hot/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap());

        verify(hotQueryService).getDetail(12L);
    }

    @Test
    void shouldRejectInvalidEnumInsteadOfReturningServerError() throws Exception {
        mockMvc.perform(get("/api/hot/list").param("source", "INVALID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("source: 参数格式不正确"));
    }

    @Test
    void shouldRejectOutOfRangeLimit() throws Exception {
        mockMvc.perform(get("/api/hot/list").param("limit", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("limit: 返回数量不能小于1"));
    }

    @Test
    void shouldValidateSearchKeyword() throws Exception {
        mockMvc.perform(get("/api/hot/search").param("keyword", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("keyword: 搜索关键词不能为空"));
    }

    @Test
    void shouldQueryTrendWithDefaults() throws Exception {
        when(hotQueryService.getTrendPoints(argThat(id -> id == 3L),
                argThat(query -> query.getHours() == 24 && query.getLimit() == 20)))
                .thenReturn(List.of(new HotTrendPointVO(100L, 1, java.time.LocalDateTime.now())));

        mockMvc.perform(get("/api/hot/3/trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    private HotItemVO item() {
        return new HotItemVO(1L, "title", "description", HotSource.WEIBO, "微博", "https://weibo.com",
                com.boilingpoint.news.common.enums.HotCategory.TECHNOLOGY, "科技", 100L, "100", 1,
                null, 0, com.boilingpoint.news.common.enums.HotTrend.STABLE, null,
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
    }
}
