package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.collector.tieba.enabled", havingValue = "true", matchIfMissing = true)
public class TiebaHotSearchCollector extends AbstractPublicHotSearchCollector {

    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;

    public TiebaHotSearchCollector(ObjectMapper objectMapper, BaiduCategoryClassifier categoryClassifier,
            @Value("${app.collector.tieba.url:https://tieba.baidu.com/hottopic/browse/topicList}") String url,
            @Value("${app.collector.tieba.limit:30}") int limit,
            @Value("${app.collector.tieba.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
        this.objectMapper = objectMapper;
        this.categoryClassifier = categoryClassifier;
    }

    @Override public HotSource source() { return HotSource.TIEBA; }

    @Override
    List<CollectedHotItem> parse(String body) {
        JsonNode data;
        try { data = objectMapper.readTree(body).path("data").path("bang_topic").path("topic_list"); }
        catch (Exception exception) { throw new IllegalStateException("Tieba hot board data is invalid JSON", exception); }
        if (!data.isArray()) throw new IllegalStateException("Tieba hot board list is missing");
        List<CollectedHotItem> items = new ArrayList<>();
        int fallbackRank = 1;
        for (JsonNode item : data) {
            String id = text(item, "topic_id");
            String title = text(item, "topic_name");
            String detailUrl = text(item, "topic_url");
            if (id == null || title == null || detailUrl == null) continue;
            detailUrl = Parser.unescapeEntities(detailUrl, false);
            String description = text(item, "topic_desc");
            long discussCount = Math.max(0, item.path("discuss_num").asLong());
            int rank = Math.max(1, item.path("idx_num").asInt(fallbackRank));
            items.add(new CollectedHotItem("tieba-" + id, title, description, source(),
                    categoryClassifier.classify(title, description), discussCount,
                    discussCount > 0 ? discussCount + " 讨论" : "热议", rank, detailUrl,
                    text(item, "topic_pic"), null));
            fallbackRank++;
            if (items.size() >= limit) break;
        }
        return items;
    }
}
