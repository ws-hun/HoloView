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
@ConditionalOnProperty(name = "app.collector.steam.enabled", havingValue = "true", matchIfMissing = true)
public class SteamHotSearchCollector extends AbstractPublicHotSearchCollector {

    public SteamHotSearchCollector(
            @Value("${app.collector.steam.url:https://store.steampowered.com/stats/stats/}") String url,
            @Value("${app.collector.steam.limit:30}") int limit,
            @Value("${app.collector.steam.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
    }

    @Override public HotSource source() { return HotSource.STEAM; }

    @Override
    List<CollectedHotItem> parse(String body) {
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (Element row : Jsoup.parse(body).select("tr.player_count_row")) {
            Element link = row.selectFirst("a.gameLink[href]");
            Element current = row.selectFirst("td:first-child .currentServers");
            if (link == null || current == null) continue;
            String title = link.text().trim();
            String href = link.attr("href");
            long players = parseNumber(current.text());
            if (title.isBlank() || href.isBlank()) continue;
            items.add(new CollectedHotItem(stableKey("steam-", href), title, null, source(), HotCategory.GAMING,
                    players, players > 0 ? players + " 人在线" : "热门游戏", rank++, href, null, null));
            if (items.size() >= limit) break;
        }
        return items;
    }

    private long parseNumber(String value) {
        try { return Long.parseLong(value.replaceAll("[^0-9]", "")); }
        catch (NumberFormatException exception) { return 0; }
    }
}
