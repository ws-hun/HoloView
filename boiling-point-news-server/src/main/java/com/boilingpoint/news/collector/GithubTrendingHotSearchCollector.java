package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.collector.github-trending.enabled", havingValue = "true", matchIfMissing = true)
public class GithubTrendingHotSearchCollector extends AbstractPublicHotSearchCollector {

    public GithubTrendingHotSearchCollector(
            @Value("${app.collector.github-trending.url:https://github.com/trending?spoken_language_code=}") String url,
            @Value("${app.collector.github-trending.limit:25}") int limit,
            @Value("${app.collector.github-trending.timeout:15s}") Duration timeout) {
        super(url, limit, timeout);
    }

    @Override public HotSource source() { return HotSource.GITHUB_TRENDING; }

    @Override
    List<CollectedHotItem> parse(String body) {
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (Element article : Jsoup.parse(body, "https://github.com").select("article.Box-row")) {
            Element link = article.selectFirst("h2 a[href]");
            if (link == null) continue;
            String repository = link.text().replaceAll("\\s+", "").trim();
            String href = link.absUrl("href");
            if (repository.isBlank() || href.isBlank()) continue;
            Element starLink = article.selectFirst("a[href$=/stargazers]");
            long stars = starLink == null ? 0 : parseNumber(starLink.text());
            Element description = article.selectFirst("p");
            items.add(new CollectedHotItem("github-trending-" + repository, repository,
                    description == null ? null : description.text().trim(), source(), HotCategory.TECHNOLOGY,
                    stars, stars > 0 ? stars + " Stars" : "Trending", rank++, href, null, null));
            if (items.size() >= limit) break;
        }
        return items;
    }

    private long parseNumber(String value) {
        try { return Long.parseLong(value.replaceAll("[^0-9]", "")); }
        catch (NumberFormatException exception) { return 0; }
    }
}
