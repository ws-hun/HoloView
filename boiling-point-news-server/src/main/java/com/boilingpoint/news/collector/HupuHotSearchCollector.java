package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.collector.hupu.enabled", havingValue = "true", matchIfMissing = true)
public class HupuHotSearchCollector extends AbstractPublicHotSearchCollector {

    public HupuHotSearchCollector(
            @Value("${app.collector.hupu.url:https://bbs.hupu.com/topic-daily-hot}") String url,
            @Value("${app.collector.hupu.limit:20}") int limit,
            @Value("${app.collector.hupu.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
    }

    @Override public HotSource source() { return HotSource.HUPU; }

    @Override protected HttpRequest.Builder customize(HttpRequest.Builder builder) {
        return super.customize(builder).header("Referer", "https://bbs.hupu.com/");
    }

    @Override
    List<CollectedHotItem> parse(String body) {
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (Element row : Jsoup.parse(body, "https://bbs.hupu.com").select("li.bbs-sl-web-post-body")) {
            Element link = row.selectFirst("a.p-title[href]");
            if (link == null) continue;
            String title = link.text().trim();
            String href = link.absUrl("href");
            if (title.isBlank() || href.isBlank()) continue;
            items.add(new CollectedHotItem(stableKey("hupu-", href), title, null, source(), HotCategory.SPORTS,
                    0L, "每日热帖", rank++, href, null, null));
            if (items.size() >= limit) break;
        }
        return items;
    }
}
