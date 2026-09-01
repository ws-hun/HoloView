package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.collector.dongqiudi.enabled", havingValue = "true", matchIfMissing = true)
public class DongqiudiHotSearchCollector extends AbstractPublicHotSearchCollector {

    private final ObjectMapper objectMapper;

    public DongqiudiHotSearchCollector(ObjectMapper objectMapper,
            @Value("${app.collector.dongqiudi.url:https://api.dongqiudi.com/app/tabs/web/1.json}") String url,
            @Value("${app.collector.dongqiudi.limit:20}") int limit,
            @Value("${app.collector.dongqiudi.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
        this.objectMapper = objectMapper;
    }

    @Override public HotSource source() { return HotSource.DONGQIUDI; }

    @Override
    List<CollectedHotItem> parse(String body) {
        JsonNode data;
        try { data = objectMapper.readTree(body).path("articles"); }
        catch (Exception exception) { throw new IllegalStateException("Dongqiudi feed data is invalid JSON", exception); }
        if (!data.isArray()) throw new IllegalStateException("Dongqiudi feed list is missing");
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : data) {
            if (item.path("is_business_ad").asInt() == 1) continue;
            String id = text(item, "id");
            String title = text(item, "title");
            String detailUrl = text(item, "share");
            if (id == null || title == null) continue;
            if (detailUrl == null) detailUrl = "https://www.dongqiudi.com/article/" + id;
            long comments = Math.max(0, item.path("comments_total").asLong());
            items.add(new CollectedHotItem("dongqiudi-" + id, title, text(item, "description"), source(),
                    HotCategory.SPORTS, comments, comments > 0 ? comments + " 评论" : "足球资讯", rank++,
                    detailUrl, text(item, "thumb"), parseDate(text(item, "created_at"))));
            if (items.size() >= limit) break;
        }
        return items;
    }

    private LocalDateTime parseDate(String value) {
        try { return value == null ? null : LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
        catch (Exception exception) { return null; }
    }
}
