package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.collector.douban-movie.enabled", havingValue = "true", matchIfMissing = true)
public class DoubanMovieHotSearchCollector extends AbstractPublicHotSearchCollector {

    private final ObjectMapper objectMapper;

    public DoubanMovieHotSearchCollector(ObjectMapper objectMapper,
            @Value("${app.collector.douban-movie.url:https://m.douban.com/rexxar/api/v2/subject/recent_hot/movie}") String url,
            @Value("${app.collector.douban-movie.limit:20}") int limit,
            @Value("${app.collector.douban-movie.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
        this.objectMapper = objectMapper;
    }

    @Override public HotSource source() { return HotSource.DOUBAN_MOVIE; }

    @Override protected HttpRequest.Builder customize(HttpRequest.Builder builder) {
        return super.customize(builder).header("Referer", "https://movie.douban.com/");
    }

    @Override
    List<CollectedHotItem> parse(String body) {
        JsonNode data;
        try { data = objectMapper.readTree(body).path("items"); }
        catch (Exception exception) { throw new IllegalStateException("Douban movie hot data is invalid JSON", exception); }
        if (!data.isArray()) throw new IllegalStateException("Douban movie hot list is missing");
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : data) {
            String id = text(item, "id");
            String title = text(item, "title");
            if (id == null || title == null) continue;
            long ratingCount = Math.max(0, item.path("rating").path("count").asLong());
            double rating = item.path("rating").path("value").asDouble();
            String ratingText = rating > 0 ? String.format("%.1f 分", rating) : "近期热门";
            items.add(new CollectedHotItem("douban-movie-" + id, title, text(item, "card_subtitle"),
                    source(), HotCategory.ENTERTAINMENT, ratingCount, ratingText, rank++,
                    "https://movie.douban.com/subject/" + id + "/", text(item.path("pic"), "large"), null));
            if (items.size() >= limit) break;
        }
        return items;
    }
}
