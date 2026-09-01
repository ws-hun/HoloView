package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class PublicHotSearchCollectorsTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(1);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldParseNowcoderHotSearchAndUseDirectDetails() {
        var collector = new NowcoderHotSearchCollector(objectMapper, "https://example.com", 1, TIMEOUT);

        var result = collector.parse("""
                {"data":{"result":[
                  {"type":74,"id":"101","uuid":"feed-101","title":"AI 编程实战","hotValueFromDolphin":19583},
                  {"type":0,"id":"102","title":"不应超过限制"}
                ]}}
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.NOWCODER);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.TECHNOLOGY);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://www.nowcoder.com/feed/main/detail/feed-101");
        assertThat(result.get(0).hotValue()).isEqualTo(19_583L);
        assertThat(result.get(0).rank()).isEqualTo(1);
    }

    @Test
    void shouldParseTiebaRankAndDecodeDirectUrl() {
        var collector = new TiebaHotSearchCollector(objectMapper, new BaiduCategoryClassifier(),
                "https://example.com", 2, TIMEOUT);

        var result = collector.parse("""
                {"data":{"bang_topic":{"topic_list":[{
                  "topic_id":"201","topic_name":"国产芯片新进展","topic_desc":"科技产业讨论",
                  "topic_url":"https://tieba.baidu.com/p/201?foo=1&amp;bar=2",
                  "topic_pic":"https://img.example/201.jpg","discuss_num":1801380,"idx_num":3
                }]}}}
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.TIEBA);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.TECHNOLOGY);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://tieba.baidu.com/p/201?foo=1&bar=2");
        assertThat(result.get(0).hotValue()).isEqualTo(1_801_380L);
        assertThat(result.get(0).rank()).isEqualTo(3);
    }

    @Test
    void shouldParseHupuPublicHotPage() {
        var collector = new HupuHotSearchCollector("https://example.com", 2, TIMEOUT);

        var result = collector.parse("""
                <ul><li class="bbs-sl-web-post-body">
                  <a class="p-title" href="/123456.html">中国队晋级决赛</a>
                </li></ul>
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.HUPU);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.SPORTS);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://bbs.hupu.com/123456.html");
        assertThat(result.get(0).hotValueText()).isEqualTo("每日热帖");
        assertThat(result.get(0).sourceItemKey()).hasSizeLessThanOrEqualTo(128);
    }

    @Test
    void shouldParseDoubanMovieRankingAndDirectSubject() {
        var collector = new DoubanMovieHotSearchCollector(objectMapper, "https://example.com", 2, TIMEOUT);

        var result = collector.parse("""
                {"items":[{
                  "id":"36812879","title":"抓特务","card_subtitle":"2026 / 中国大陆 / 剧情",
                  "rating":{"count":102935,"value":7.4},
                  "pic":{"large":"https://img.example/poster.jpg"}
                }]}
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.DOUBAN_MOVIE);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.ENTERTAINMENT);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://movie.douban.com/subject/36812879/");
        assertThat(result.get(0).hotValue()).isEqualTo(102_935L);
        assertThat(result.get(0).hotValueText()).isEqualTo("7.4 分");
    }

    @Test
    void shouldParseGithubTrendingRepositories() {
        var collector = new GithubTrendingHotSearchCollector("https://example.com", 2, TIMEOUT);

        var result = collector.parse("""
                <article class="Box-row">
                  <h2><a href="/openai/codex"> openai / codex </a></h2>
                  <p>Lightweight coding agent</p>
                  <a href="/openai/codex/stargazers"> 12,345 </a>
                </article>
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.GITHUB_TRENDING);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.TECHNOLOGY);
        assertThat(result.get(0).title()).isEqualTo("openai/codex");
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://github.com/openai/codex");
        assertThat(result.get(0).hotValue()).isEqualTo(12_345L);
    }

    @Test
    void shouldParseSteamConcurrentPlayers() {
        var collector = new SteamHotSearchCollector("https://example.com", 2, TIMEOUT);

        var result = collector.parse("""
                <table><tr class="player_count_row">
                  <td><span class="currentServers">666,154</span></td>
                  <td><a class="gameLink" href="https://store.steampowered.com/app/730/">Counter-Strike 2</a></td>
                </tr></table>
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.STEAM);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.GAMING);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://store.steampowered.com/app/730/");
        assertThat(result.get(0).hotValue()).isEqualTo(666_154L);
        assertThat(result.get(0).sourceItemKey()).hasSizeLessThanOrEqualTo(128);
    }

    @Test
    void shouldParseSmzdmPublicHotArticles() {
        var collector = new SmzdmHotSearchCollector("https://example.com", "https://example.com/feed", 2, TIMEOUT);

        var result = collector.parse("""
                <div id="feed-main-list"><h5 class="z-feed-title">
                  <a href="/p/a1b2c3/">高性价比家电选购指南</a>
                </h5></div>
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.SMZDM);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.LIFESTYLE);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://post.smzdm.com/p/a1b2c3/");
        assertThat(result.get(0).hotValueText()).isEqualTo("热门文章");
        assertThat(result.get(0).sourceItemKey()).hasSizeLessThanOrEqualTo(128);
    }

    @Test
    void shouldParseSmzdmPublicFeedFallback() {
        var collector = new SmzdmHotSearchCollector("https://example.com", "https://example.com/feed", 2, TIMEOUT);

        var result = collector.parse("""
                <?xml version="1.0"?><rss><channel><item>
                  <title>公开 RSS 新文章</title>
                  <link>https://post.smzdm.com/p/rss-101/</link>
                  <description><![CDATA[<p>文章摘要</p>]]></description>
                </item></channel></rss>
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://post.smzdm.com/p/rss-101/");
        assertThat(result.get(0).description()).isEqualTo("文章摘要");
        assertThat(result.get(0).hotValueText()).isEqualTo("最新文章");
    }

    @Test
    void shouldParseDongqiudiFeedAndSkipAdvertisements() {
        var collector = new DongqiudiHotSearchCollector(objectMapper, "https://example.com", 2, TIMEOUT);

        var result = collector.parse("""
                {"articles":[
                  {"id":"301","title":"广告","is_business_ad":1},
                  {"id":"302","title":"国足公布新名单","description":"年轻球员入选",
                   "comments_total":975,"share":"https://www.dongqiudi.com/article/302",
                   "thumb":"https://img.example/302.jpg","created_at":"2026-09-01 14:53:41","is_business_ad":0}
                ]}
                """);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo(HotSource.DONGQIUDI);
        assertThat(result.get(0).category()).isEqualTo(HotCategory.SPORTS);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://www.dongqiudi.com/article/302");
        assertThat(result.get(0).hotValue()).isEqualTo(975L);
        assertThat(result.get(0).publishedAt()).isEqualTo(LocalDateTime.of(2026, 9, 1, 14, 53, 41));
    }

    @Test
    void shouldRejectJsonPayloadsWithoutExpectedLists() {
        assertThatIllegalStateException()
                .isThrownBy(() -> new NowcoderHotSearchCollector(objectMapper, "https://example.com", 1, TIMEOUT)
                        .parse("{}"))
                .withMessage("Nowcoder hot board list is missing");
        assertThatIllegalStateException()
                .isThrownBy(() -> new TiebaHotSearchCollector(objectMapper, new BaiduCategoryClassifier(),
                        "https://example.com", 1, TIMEOUT).parse("{}"))
                .withMessage("Tieba hot board list is missing");
        assertThatIllegalStateException()
                .isThrownBy(() -> new DoubanMovieHotSearchCollector(objectMapper,
                        "https://example.com", 1, TIMEOUT).parse("{}"))
                .withMessage("Douban movie hot list is missing");
        assertThatIllegalStateException()
                .isThrownBy(() -> new DongqiudiHotSearchCollector(objectMapper,
                        "https://example.com", 1, TIMEOUT).parse("{}"))
                .withMessage("Dongqiudi feed list is missing");
    }
}
