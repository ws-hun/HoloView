package com.boilingpoint.news.service;

import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.entity.HotItemEntity;
import com.boilingpoint.news.exception.BusinessException;
import com.boilingpoint.news.mapper.HotItemMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HotSourceLinkServiceTest {

    private final HotItemMapper hotItemMapper = mock(HotItemMapper.class);
    private final HttpClient httpClient = mock(HttpClient.class);
    private final HotSourceLinkService service = new HotSourceLinkService(
            hotItemMapper, httpClient, Duration.ofSeconds(1), Duration.ofMinutes(1));

    @Test
    void shouldPreferPublisherPageOverBaiduContentAndSearchLinks() {
        String html = """
                <div id="content_left">
                  <h3><a href="https://www.baidu.com/s?wd=topic">search</a></h3>
                  <h3><a href="https://baijiahao.baidu.com/s?id=1">baidu content</a></h3>
                  <h3><a href="https://news.example.com/story/1">publisher</a></h3>
                </div>
                """;

        assertThat(service.parseNewsLink(html))
                .isEqualTo(URI.create("https://news.example.com/story/1"));
    }

    @Test
    void shouldRejectPageWithoutContentLinksInsteadOfReturningSearch() {
        String html = "<div id='content_left'><h3><a href='https://www.baidu.com/s?wd=topic'>search</a></h3></div>";

        assertThatThrownBy(() -> service.parseNewsLink(html))
                .isInstanceOf(BusinessException.class)
                .hasMessage("未找到可用的报道详情地址");
    }

    @Test
    void shouldUseExistingPublisherLinkWithoutNewsLookup() {
        HotItemEntity item = HotItemEntity.builder()
                .id(7L)
                .source(HotSource.WEIBO)
                .sourceUrl("https://weibo.com/story/7")
                .status(1)
                .build();
        when(hotItemMapper.selectById(7L)).thenReturn(item);

        assertThat(service.resolve(7L)).isEqualTo(URI.create("https://weibo.com/story/7"));
        verifyNoInteractions(httpClient);
    }
}
