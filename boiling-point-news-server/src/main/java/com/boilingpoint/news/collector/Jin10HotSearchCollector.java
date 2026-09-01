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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.jin10.enabled", havingValue = "true", matchIfMissing = true)
public class Jin10HotSearchCollector implements HotSearchCollector {

    private static final String USER_AGENT = "HoloView/0.1 (+https://github.com/ws-hun/HoloView)";
    private static final DateTimeFormatter SOURCE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final HttpClient httpClient;
    private final String url;
    private final int limit;
    private final Duration requestTimeout;

    public Jin10HotSearchCollector(ObjectMapper objectMapper, BaiduCategoryClassifier categoryClassifier,
                                   @Value("${app.collector.jin10.url:https://www.jin10.com/flash_newest.js}") String url,
                                   @Value("${app.collector.jin10.limit:30}") int limit,
                                   @Value("${app.collector.jin10.timeout:10s}") Duration requestTimeout) {
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
        return HotSource.JIN10;
    }

    @Override
    public List<CollectedHotItem> collect() {
        long startedAt = System.nanoTime();
        String requestUrl = url + (url.contains("?") ? "&" : "?") + "t=" + System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl)).timeout(requestTimeout)
                .header("User-Agent", USER_AGENT).header("Accept", "application/javascript,text/plain,*/*")
                .GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Jin10 hot board returned HTTP " + response.statusCode());
            }
            List<CollectedHotItem> items = parse(response.body());
            log.info("Jin10 hot board collected: itemCount={}, responseBytes={}, durationMs={}", items.size(),
                    response.body().getBytes(StandardCharsets.UTF_8).length, elapsedMillis(startedAt));
            return items;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Jin10 hot board request interrupted", exception);
        } catch (IOException exception) {
            log.warn("Jin10 hot board request failed: durationMs={}, error={}", elapsedMillis(startedAt), exception.toString());
            throw new IllegalStateException("Jin10 hot board request failed", exception);
        }
    }

    List<CollectedHotItem> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Jin10 hot board data is empty");
        }
        String json = raw.trim().replaceFirst("^var\\s+newest\\s*=\\s*", "").replaceFirst(";+$", "").trim();
        final JsonNode data;
        try {
            data = objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Jin10 hot board data is invalid JSON", exception);
        }
        if (!data.isArray()) {
            throw new IllegalStateException("Jin10 hot board list is missing");
        }
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : data) {
            if (containsChannel(item.path("channel"), 5)) {
                continue;
            }
            JsonNode detail = item.path("data");
            String rawText = firstText(detail, "title", "content");
            String id = text(item, "id");
            if (rawText == null || id == null) {
                continue;
            }
            String cleanText = Jsoup.parse(rawText.replace("<b>", "").replace("</b>", "")).text().trim();
            String title = cleanText;
            String description = null;
            if (cleanText.startsWith("【")) {
                int end = cleanText.indexOf('】');
                if (end > 1) {
                    title = cleanText.substring(1, end).trim();
                    description = cleanText.substring(end + 1).trim();
                }
            }
            if (title.isBlank()) {
                continue;
            }
            boolean important = item.path("important").asInt(0) > 0;
            String hotText = important ? "重要快讯" : "快讯";
            LocalDateTime published = parseTime(text(item, "time"));
            items.add(new CollectedHotItem("jin10-" + id, title, description, source(),
                    categoryClassifier.classify(title, description), 0L, hotText, rank++,
                    "https://flash.jin10.com/detail/" + id, validUrl(text(detail, "pic")), published));
            if (items.size() >= limit) {
                break;
            }
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Jin10 hot board contains no usable items");
        }
        return List.copyOf(items);
    }

    private boolean containsChannel(JsonNode channels, int excluded) {
        if (!channels.isArray()) return false;
        for (JsonNode channel : channels) if (channel.asInt(-1) == excluded) return true;
        return false;
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (value != null) return value;
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDateTime parseTime(String value) {
        try { return value == null ? null : LocalDateTime.parse(value, SOURCE_TIME); }
        catch (RuntimeException ignored) { return null; }
    }

    private String validUrl(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            URI uri = URI.create(value);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null ? uri.toString() : null;
        } catch (IllegalArgumentException ignored) { return null; }
    }

    private long elapsedMillis(long startedAt) { return (System.nanoTime() - startedAt) / 1_000_000; }
}
