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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.hacker-news.enabled", havingValue = "true", matchIfMissing = true)
public class HackerNewsHotSearchCollector implements HotSearchCollector {

    private static final String USER_AGENT = "HoloView/0.1 (+https://github.com/ws-hun/HoloView)";
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Shanghai");
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final BaiduCategoryClassifier categoryClassifier;
    private final String topStoriesUrl;
    private final String itemUrl;
    private final int limit;
    private final Duration requestTimeout;

    public HackerNewsHotSearchCollector(ObjectMapper objectMapper, BaiduCategoryClassifier categoryClassifier,
                                        @Value("${app.collector.hacker-news.url:https://hacker-news.firebaseio.com/v0/topstories.json}") String topStoriesUrl,
                                        @Value("${app.collector.hacker-news.item-url:https://hacker-news.firebaseio.com/v0/item/}") String itemUrl,
                                        @Value("${app.collector.hacker-news.limit:30}") int limit,
                                        @Value("${app.collector.hacker-news.timeout:10s}") Duration requestTimeout) {
        this.objectMapper = objectMapper;
        this.categoryClassifier = categoryClassifier;
        this.topStoriesUrl = topStoriesUrl;
        this.itemUrl = itemUrl;
        this.limit = Math.max(1, Math.min(50, limit));
        this.requestTimeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public HotSource source() {
        return HotSource.HACKER_NEWS;
    }

    @Override
    public List<CollectedHotItem> collect() {
        long startedAt = System.nanoTime();
        try {
            JsonNode ids = getJson(topStoriesUrl);
            if (!ids.isArray()) {
                throw new IllegalStateException("Hacker News top stories list is missing");
            }
            List<CompletableFuture<JsonNode>> futures = new ArrayList<>();
            int count = Math.min(limit, ids.size());
            for (int index = 0; index < count; index++) {
                futures.add(getJsonAsync(itemEndpoint(ids.get(index).asText())));
            }
            List<CollectedHotItem> items = new ArrayList<>();
            int rank = 1;
            int failedItemCount = 0;
            for (CompletableFuture<JsonNode> future : futures) {
                JsonNode item;
                try {
                    item = future.join();
                } catch (RuntimeException exception) {
                    failedItemCount++;
                    continue;
                }
                CollectedHotItem collectedItem = toCollectedItem(item, rank);
                if (collectedItem != null) {
                    items.add(collectedItem);
                    rank++;
                }
            }
            if (items.isEmpty()) {
                throw new IllegalStateException("Hacker News hot board contains no usable items");
            }
            if (failedItemCount > 0) {
                log.warn("Hacker News item requests partially failed: failedCount={}, requestedCount={}",
                        failedItemCount, futures.size());
            }
            log.info("Hacker News hot board collected: itemCount={}, durationMs={}",
                    items.size(), elapsedMillis(startedAt));
            return List.copyOf(items);
        } catch (IOException exception) {
            log.warn("Hacker News hot board request failed: durationMs={}, error={}",
                    elapsedMillis(startedAt), exception.toString());
            throw new IllegalStateException("Hacker News hot board request failed", exception);
        }
    }

    List<CollectedHotItem> parse(String topStoriesJson, List<String> itemJsons) {
        final JsonNode ids;
        try {
            ids = objectMapper.readTree(topStoriesJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Hacker News top stories data is invalid JSON", exception);
        }
        if (!ids.isArray()) {
            throw new IllegalStateException("Hacker News top stories list is missing");
        }
        Map<String, JsonNode> itemById = new HashMap<>();
        for (String itemJson : itemJsons) {
            try {
                JsonNode item = objectMapper.readTree(itemJson);
                String id = text(item, "id");
                if (id != null) {
                    itemById.put(id, item);
                }
            } catch (JsonProcessingException ignored) {
                // A malformed item should not discard the rest of the public board.
            }
        }
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode idNode : ids) {
            CollectedHotItem item = toCollectedItem(itemById.get(idNode.asText()), rank);
            if (item != null) {
                items.add(item);
                rank++;
            }
            if (items.size() >= limit) {
                break;
            }
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Hacker News hot board contains no usable items");
        }
        return List.copyOf(items);
    }

    private CollectedHotItem toCollectedItem(JsonNode item, int rank) {
        if (item == null || !"story".equals(item.path("type").asText())
                || item.path("deleted").asBoolean(false) || item.path("dead").asBoolean(false)) {
            return null;
        }
        String id = text(item, "id");
        String title = text(item, "title");
        if (id == null || title == null) {
            return null;
        }
        long score = Math.max(0L, item.path("score").asLong(0L));
        String description = text(item, "text");
        LocalDateTime publishedAt = item.path("time").canConvertToLong()
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(item.path("time").asLong()), DISPLAY_ZONE)
                : null;
        return new CollectedHotItem("hacker-news-" + id, title, description, source(),
                categoryClassifier.classify(title, description), score,
                score > 0 ? score + " points" : "Hacker News", rank,
                "https://news.ycombinator.com/item?id=" + id, null, publishedAt);
    }

    private JsonNode getJson(String endpoint) throws IOException {
        try {
            HttpResponse<String> response = httpClient.send(request(endpoint), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("request interrupted", exception);
        }
    }

    private CompletableFuture<JsonNode> getJsonAsync(String endpoint) {
        return httpClient.sendAsync(request(endpoint), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new IllegalStateException("HTTP " + response.statusCode());
                    }
                    try {
                        return objectMapper.readTree(response.body());
                    } catch (JsonProcessingException exception) {
                        throw new IllegalStateException("invalid item JSON", exception);
                    }
                });
    }

    private String itemEndpoint(String id) {
        return itemUrl + id + ".json";
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private HttpRequest request(String endpoint) {
        return HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(requestTimeout)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
