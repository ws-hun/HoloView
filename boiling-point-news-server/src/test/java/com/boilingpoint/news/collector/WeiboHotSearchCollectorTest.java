package com.boilingpoint.news.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class WeiboHotSearchCollectorTest {

    @Test
    void parsesPublicSummaryRows() {
        WeiboHotSearchCollector collector = new WeiboHotSearchCollector(
                new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", "", "", 10, Duration.ofSeconds(1));
        var result = collector.parse("""
                <div id='pl_top_realtimehot'><table><tbody>
                  <tr><td class='td-01'>1</td><td class='td-02'><a href='/weibo?q=1'>测试热搜</a><span>12.5万</span></td><td class='td-03'>热</td></tr>
                </tbody></table></div>
                """);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("测试热搜");
        assertThat(result.get(0).hotValue()).isEqualTo(125_000L);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://s.weibo.com/weibo?q=1");
    }

    @Test
    void parsesFallbackItems() {
        WeiboHotSearchCollector collector = new WeiboHotSearchCollector(
                new ObjectMapper(), new BaiduCategoryClassifier(), "https://example.com", "", "", 10, Duration.ofSeconds(1));
        var result = collector.parse("""
                {"items":[{"title":"备用热搜","url":"https://weibo.com/item/1","hot":"3.2万"}]}
                """);
        assertThat(result.get(0).hotValue()).isEqualTo(32_000L);
    }
}
