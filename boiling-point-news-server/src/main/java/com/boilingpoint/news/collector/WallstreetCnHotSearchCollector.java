package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
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
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.wallstreet-cn.enabled", havingValue = "true", matchIfMissing = true)
public class WallstreetCnHotSearchCollector implements HotSearchCollector {

    private static final String USER_AGENT = "HoloView/0.1 (+https://github.com/ws-hun/HoloView)";
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Shanghai");

    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final HttpClient httpClient;
    private final String url;
    private final int limit;
    private final Duration requestTimeout;

    public WallstreetCnHotSearchCollector(
            ObjectMapper objectMapper,
            BaiduCategoryClassifier categoryClassifier,
            @Value("${app.collector.wallstreet-cn.url:https://api-one-wscn.awtmt.com/apiv1/content/lives?channel=global-channel&limit=30}") String url,
            @Value("${app.collector.wallstreet-cn.limit:30}") int limit,
            @Value("${app.collector.wallstreet-cn.timeout:10s}") Duration requestTimeout) {
        this.objectMapper = objectMapper;
        this.categoryClassifier = categoryClassifier;
        this.url = url;
        this.limit = Math.max(1, Math.min(50, limit));
        this.requestTimeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public HotSource source() {
        return HotSource.WALLSTREET_CN;
    }

    @Override
    public List<CollectedHotItem> collect() {
        long startedAt = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(requestTimeout)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("WallstreetCN live feed returned HTTP " + response.statusCode());
            }
            List<CollectedHotItem> items = parse(response.body());
            log.info("WallstreetCN live feed collected: itemCount={}, responseBytes={}, durationMs={}",
                    items.size(), response.body().getBytes(StandardCharsets.UTF_8).length,
                    elapsedMillis(startedAt));
            return items;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("WallstreetCN live feed request interrupted", exception);
        } catch (IOException exception) {
            log.warn("WallstreetCN live feed request failed: durationMs={}, error={}",
                    elapsedMillis(startedAt), exception.toString());
            throw new IllegalStateException("WallstreetCN live feed request failed", exception);
        }
    }

    List<CollectedHotItem> parse(String json) {
        final JsonNode itemsNode;
        try {
            itemsNode = objectMapper.readTree(json).path("data").path("items");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("WallstreetCN live feed data is invalid JSON", exception);
        }
        if (!itemsNode.isArray()) {
            throw new IllegalStateException("WallstreetCN live feed list is missing");
        }

        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : itemsNode) {
            String id = text(item, "id");
            String title = firstText(item, "title", "content_text");
            String sourceUrl = validWallstreetUrl(text(item, "uri"));
            if (id == null || title == null || sourceUrl == null) {
                continue;
            }
            title = Jsoup.parse(title).text().trim();
            if (title.isBlank()) {
                continue;
            }
            String description = text(item, "content_text");
            if (description != null) {
                description = Jsoup.parse(description).text().trim();
                if (description.equals(title)) {
                    description = null;
                }
            }
            LocalDateTime publishedAt = item.path("display_time").canConvertToLong()
                    ? LocalDateTime.ofInstant(Instant.ofEpochSecond(item.path("display_time").asLong()), DISPLAY_ZONE)
                    : null;
            items.add(new CollectedHotItem("wallstreet-cn-" + id, title, description, source(),
                    categoryClassifier.classify(title, description), 0L, "快讯", rank++, sourceUrl,
                    firstCover(item.path("cover_images")), publishedAt));
            if (items.size() >= limit) {
                break;
            }
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("WallstreetCN live feed contains no usable items");
        }
        return List.copyOf(items);
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String validWallstreetUrl(String value) {
        if (value == null) {
            return null;
        }
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme()) && host != null
                    && (host.equals("wallstreetcn.com") || host.endsWith(".wallstreetcn.com"))
                    ? uri.toString() : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String firstCover(JsonNode covers) {
        if (!covers.isArray() || covers.isEmpty()) {
            return null;
        }
        JsonNode first = covers.get(0);
        String value = first.isTextual() ? first.asText() : first.path("uri").asText(null);
        if (value == null) {
            value = first.path("url").asText(null);
        }
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null ? uri.toString() : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
