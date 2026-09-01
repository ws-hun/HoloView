package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.collector.smzdm.enabled", havingValue = "true", matchIfMissing = true)
public class SmzdmHotSearchCollector extends AbstractPublicHotSearchCollector {

    private final String fallbackUrl;

    public SmzdmHotSearchCollector(
            @Value("${app.collector.smzdm.url:https://post.smzdm.com/hot_1/}") String url,
            @Value("${app.collector.smzdm.fallback-url:https://post.smzdm.com/feed/}") String fallbackUrl,
            @Value("${app.collector.smzdm.limit:30}") int limit,
            @Value("${app.collector.smzdm.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
        this.fallbackUrl = fallbackUrl;
    }

    @Override public HotSource source() { return HotSource.SMZDM; }

    @Override protected HttpRequest.Builder customize(HttpRequest.Builder builder) {
        return super.customize(builder).header("Referer", "https://post.smzdm.com/");
    }

    @Override
    protected List<String> candidateUrls() {
        return fallbackUrl == null || fallbackUrl.isBlank() || fallbackUrl.equals(url)
                ? List.of(url) : List.of(url, fallbackUrl);
    }

    @Override
    List<CollectedHotItem> parse(String body) {
        if (body.stripLeading().startsWith("<?xml") || body.contains("<rss")) {
            return parseFeed(body);
        }
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (Element titleBox : Jsoup.parse(body, "https://post.smzdm.com").select("#feed-main-list .z-feed-title")) {
            Element link = titleBox.selectFirst("a[href]");
            if (link == null) continue;
            String title = link.text().trim();
            String href = link.absUrl("href");
            if (title.isBlank() || href.isBlank()) continue;
            items.add(new CollectedHotItem(stableKey("smzdm-", href), title, null, source(), HotCategory.LIFESTYLE,
                    0L, "热门文章", rank++, href, null, null));
            if (items.size() >= limit) break;
        }
        return items;
    }

    private List<CollectedHotItem> parseFeed(String body) {
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (Element entry : Jsoup.parse(body, fallbackUrl, Parser.xmlParser()).select("item")) {
            String title = entry.select("title").text().trim();
            String href = entry.select("link").text().trim();
            if (title.isBlank() || href.isBlank()) continue;
            String description = Jsoup.parse(entry.select("description").text()).text().trim();
            items.add(new CollectedHotItem(stableKey("smzdm-", href), title,
                    description.isBlank() ? null : description, source(), HotCategory.LIFESTYLE,
                    0L, "最新文章", rank++, href, null, null));
            if (items.size() >= limit) break;
        }
        return items;
    }
}
