package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.juejin.enabled", havingValue = "true", matchIfMissing = true)
public class JuejinHotSearchCollector implements HotSearchCollector {

    private static final String USER_AGENT = "HoloView/0.1 (+https://github.com/ws-hun/HoloView)";
    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final HttpClient httpClient;
    private final String url;
    private final int limit;
    private final Duration requestTimeout;

    public JuejinHotSearchCollector(ObjectMapper objectMapper, BaiduCategoryClassifier categoryClassifier,
                                    @Value("${app.collector.juejin.url:https://api.juejin.cn/content_api/v1/content/article_rank?category_id=1&type=hot&spider=0}") String url,
                                    @Value("${app.collector.juejin.limit:30}") int limit,
                                    @Value("${app.collector.juejin.timeout:10s}") Duration requestTimeout) {
        this.objectMapper = objectMapper;
        this.categoryClassifier = categoryClassifier;
        this.url = url;
        this.limit = Math.max(1, Math.min(50, limit));
        this.requestTimeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    @Override
    public HotSource source() { return HotSource.JUEJIN; }

    @Override
    public List<CollectedHotItem> collect() {
        long startedAt = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(requestTimeout)
                .header("User-Agent", USER_AGENT).header("Accept", "application/json,text/plain,*/*").GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) throw new IllegalStateException("Juejin hot board returned HTTP " + response.statusCode());
            List<CollectedHotItem> items = parse(response.body());
            log.info("Juejin hot board collected: itemCount={}, responseBytes={}, durationMs={}", items.size(),
                    response.body().getBytes(StandardCharsets.UTF_8).length, elapsedMillis(startedAt));
            return items;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Juejin hot board request interrupted", exception);
        } catch (IOException exception) {
            log.warn("Juejin hot board request failed: durationMs={}, error={}", elapsedMillis(startedAt), exception.toString());
            throw new IllegalStateException("Juejin hot board request failed", exception);
        }
    }

    List<CollectedHotItem> parse(String json) {
        final JsonNode data;
        try { data = objectMapper.readTree(json).path("data"); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Juejin hot board data is invalid JSON", exception); }
        if (!data.isArray()) throw new IllegalStateException("Juejin hot board list is missing");
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : data) {
            JsonNode content = item.path("content");
            String id = text(content, "content_id");
            String title = text(content, "title");
            if (id == null || title == null) continue;
            long hotValue = item.path("content_counter").path("hot_rank").asLong(0L);
            String description = text(content, "brief");
            items.add(new CollectedHotItem("juejin-" + id, title, description, source(),
                    categoryClassifier.classify(title, description), hotValue, hotValue > 0 ? "热榜 " + hotValue : "热榜",
                    rank++, "https://juejin.cn/post/" + id, null, null));
            if (items.size() >= limit) break;
        }
        if (items.isEmpty()) throw new IllegalStateException("Juejin hot board contains no usable items");
        return List.copyOf(items);
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }
    private long elapsedMillis(long startedAt) { return (System.nanoTime() - startedAt) / 1_000_000; }
}
