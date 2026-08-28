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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.bilibili.enabled", havingValue = "true", matchIfMissing = true)
public class BilibiliHotSearchCollector implements HotSearchCollector {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 HoloView/0.1";

    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final HttpClient httpClient;
    private final String url;
    private final int limit;
    private final Duration requestTimeout;

    public BilibiliHotSearchCollector(ObjectMapper objectMapper, BaiduCategoryClassifier categoryClassifier,
                                      @Value("${app.collector.bilibili.url:https://api.bilibili.com/x/web-interface/popular?ps=30&pn=1}") String url,
                                      @Value("${app.collector.bilibili.limit:30}") int limit,
                                      @Value("${app.collector.bilibili.timeout:10s}") Duration requestTimeout) {
        this.objectMapper = objectMapper;
        this.categoryClassifier = categoryClassifier;
        this.url = url;
        this.limit = Math.max(1, Math.min(50, limit));
        this.requestTimeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    @Override
    public HotSource source() {
        return HotSource.BILIBILI;
    }

    @Override
    public List<CollectedHotItem> collect() {
        long startedAt = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(requestTimeout)
                .header("User-Agent", USER_AGENT).header("Accept", "application/json,text/plain,*/*")
                .header("Referer", "https://www.bilibili.com/").GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Bilibili hot board returned HTTP " + response.statusCode());
            }
            List<CollectedHotItem> items = parse(response.body());
            log.info("Bilibili hot board collected: itemCount={}, responseBytes={}, durationMs={}", items.size(),
                    response.body().getBytes(StandardCharsets.UTF_8).length, elapsedMillis(startedAt));
            return items;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bilibili hot board request interrupted", exception);
        } catch (IOException exception) {
            log.warn("Bilibili hot board request failed: durationMs={}, error={}", elapsedMillis(startedAt), exception.toString());
            throw new IllegalStateException("Bilibili hot board request failed", exception);
        }
    }

    List<CollectedHotItem> parse(String json) {
        final JsonNode list;
        try {
            list = objectMapper.readTree(json).path("data").path("list");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Bilibili hot board data is invalid JSON", exception);
        }
        if (!list.isArray()) {
            throw new IllegalStateException("Bilibili hot board list is missing");
        }
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : list) {
            String bvid = text(item, "bvid");
            String title = text(item, "title");
            if (bvid == null || title == null) {
                continue;
            }
            String description = text(item, "desc");
            long hotValue = item.path("stat").path("view").asLong(0L);
            String owner = text(item.path("owner"), "name");
            String hotText = hotValue > 0 ? formatHotValue(hotValue) + "播放" : owner;
            items.add(new CollectedHotItem("bilibili-" + bvid, title, description, source(),
                    categoryClassifier.classify(title, description), hotValue, hotText, rank++,
                    "https://www.bilibili.com/video/" + bvid, validHttpUrl(text(item, "pic")), null));
            if (items.size() >= limit) {
                break;
            }
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Bilibili hot board contains no usable items");
        }
        return List.copyOf(items);
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String validHttpUrl(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            URI uri = URI.create(value.trim());
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) && uri.getHost() != null
                    ? uri.toString() : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String formatHotValue(long value) {
        if (value >= 100_000_000) return String.format("%.2f亿", value / 100_000_000.0);
        if (value >= 10_000) return String.format("%.1f万", value / 10_000.0);
        return Long.toString(value);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
