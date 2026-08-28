package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.toutiao.enabled", havingValue = "true", matchIfMissing = true)
public class ToutiaoHotSearchCollector implements HotSearchCollector {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 HoloView/0.1";

    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final HttpClient httpClient;
    private final String url;
    private final int limit;
    private final Duration requestTimeout;

    public ToutiaoHotSearchCollector(
            ObjectMapper objectMapper,
            BaiduCategoryClassifier categoryClassifier,
            @Value("${app.collector.toutiao.url:https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc}") String url,
            @Value("${app.collector.toutiao.limit:30}") int limit,
            @Value("${app.collector.toutiao.timeout:10s}") Duration requestTimeout) {
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
        return HotSource.TOUTIAO;
    }

    @Override
    public List<CollectedHotItem> collect() {
        long startedAt = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(requestTimeout)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json,text/plain,*/*")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Toutiao hot board returned HTTP " + response.statusCode());
            }
            List<CollectedHotItem> items = parse(response.body());
            log.info("Toutiao hot board collected: itemCount={}, responseBytes={}, durationMs={}",
                    items.size(), response.body().getBytes(StandardCharsets.UTF_8).length,
                    elapsedMillis(startedAt));
            return items;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Toutiao hot board request interrupted", exception);
        } catch (IOException exception) {
            log.warn("Toutiao hot board request failed: durationMs={}, error={}",
                    elapsedMillis(startedAt), exception.toString());
            throw new IllegalStateException("Toutiao hot board request failed", exception);
        }
    }

    List<CollectedHotItem> parse(String json) {
        final JsonNode data;
        try {
            data = objectMapper.readTree(json).path("data");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Toutiao hot board data is invalid JSON", exception);
        }
        if (!data.isArray()) {
            throw new IllegalStateException("Toutiao hot board list is missing");
        }
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : data) {
            String title = text(item, "Title");
            String sourceUrl = text(item, "Url");
            if (title == null || sourceUrl == null || !sourceUrl.startsWith("http")) {
                continue;
            }
            String description = text(item, "QueryWord");
            long hotScore = positiveLong(item.path("HotValue").asText());
            String clusterId = text(item, "ClusterIdStr");
            items.add(new CollectedHotItem(
                    stableKey(clusterId == null ? title : clusterId), title, description, source(),
                    category(item, title, description), hotScore, formatHotValue(hotScore), rank++, sourceUrl,
                    item.path("Image").path("url").asText(null), null));
            if (items.size() >= limit) {
                break;
            }
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Toutiao hot board contains no usable items");
        }
        return List.copyOf(items);
    }

    private HotCategory category(JsonNode item, String title, String description) {
        String interest = item.path("InterestCategory").path(0).asText("").toLowerCase();
        return switch (interest) {
            case "technology" -> HotCategory.TECHNOLOGY;
            case "finance" -> HotCategory.FINANCE;
            case "entertainment" -> HotCategory.ENTERTAINMENT;
            case "sports" -> HotCategory.SPORTS;
            case "car" -> HotCategory.AUTOMOTIVE;
            case "international", "military", "taiwan" -> HotCategory.INTERNATIONAL;
            case "game" -> HotCategory.GAMING;
            case "health", "food", "travel", "education" -> HotCategory.LIFESTYLE;
            default -> categoryClassifier.classify(title, description);
        };
    }

    private String stableKey(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.trim().getBytes(StandardCharsets.UTF_8));
            return "toutiao-" + HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private long positiveLong(String value) {
        try {
            return Math.max(0L, Long.parseLong(value));
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private String formatHotValue(long value) {
        if (value >= 100_000_000) {
            return String.format("%.2f亿", value / 100_000_000.0);
        }
        if (value >= 10_000) {
            return String.format("%.1f万", value / 10_000.0);
        }
        return Long.toString(value);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
