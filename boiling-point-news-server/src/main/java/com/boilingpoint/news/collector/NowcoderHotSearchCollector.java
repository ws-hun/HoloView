package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.collector.nowcoder.enabled", havingValue = "true", matchIfMissing = true)
public class NowcoderHotSearchCollector extends AbstractPublicHotSearchCollector {

    private final ObjectMapper objectMapper;

    public NowcoderHotSearchCollector(ObjectMapper objectMapper,
            @Value("${app.collector.nowcoder.url:https://gw-c.nowcoder.com/api/sparta/hot-search/top-hot-pc?size=20&t=}") String url,
            @Value("${app.collector.nowcoder.limit:20}") int limit,
            @Value("${app.collector.nowcoder.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
        this.objectMapper = objectMapper;
    }

    @Override public HotSource source() { return HotSource.NOWCODER; }

    @Override
    List<CollectedHotItem> parse(String body) {
        JsonNode data;
        try { data = objectMapper.readTree(body).path("data").path("result"); }
        catch (Exception exception) { throw new IllegalStateException("Nowcoder hot board data is invalid JSON", exception); }
        if (!data.isArray()) throw new IllegalStateException("Nowcoder hot board list is missing");
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : data) {
            String id = text(item, "id");
            String uuid = text(item, "uuid");
            String title = text(item, "title");
            if (id == null || title == null) continue;
            String detailUrl = item.path("type").asInt() == 74 && uuid != null
                    ? "https://www.nowcoder.com/feed/main/detail/" + uuid
                    : "https://www.nowcoder.com/discuss/" + id;
            long hotValue = Math.max(0, item.path("hotValueFromDolphin").asLong());
            items.add(new CollectedHotItem("nowcoder-" + id, title, null, source(), HotCategory.TECHNOLOGY,
                    hotValue, hotValue > 0 ? hotValue + " 热度" : "热搜", rank++, detailUrl, null, null));
            if (items.size() >= limit) break;
        }
        return items;
    }
}
