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
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.the-paper.enabled", havingValue = "true", matchIfMissing = true)
public class ThePaperHotSearchCollector implements HotSearchCollector {
    private static final String USER_AGENT = "HoloView/0.1 (+https://github.com/ws-hun/HoloView)";
    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final HttpClient httpClient;
    private final String url;
    private final int limit;
    private final Duration requestTimeout;

    public ThePaperHotSearchCollector(ObjectMapper objectMapper, BaiduCategoryClassifier categoryClassifier,
                                      @Value("${app.collector.the-paper.url:https://cache.thepaper.cn/contentapi/wwwIndex/rightSidebar}") String url,
                                      @Value("${app.collector.the-paper.limit:30}") int limit,
                                      @Value("${app.collector.the-paper.timeout:10s}") Duration requestTimeout) {
        this.objectMapper = objectMapper; this.categoryClassifier = categoryClassifier; this.url = url;
        this.limit = Math.max(1, Math.min(50, limit)); this.requestTimeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NORMAL).build();
    }
    @Override public HotSource source() { return HotSource.THE_PAPER; }
    @Override public List<CollectedHotItem> collect() {
        long started = System.nanoTime();
        try {
            HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder(URI.create(url)).timeout(requestTimeout)
                    .header("User-Agent", USER_AGENT).header("Accept", "application/json,text/plain,*/*").GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) throw new IllegalStateException("ThePaper hot board returned HTTP " + response.statusCode());
            List<CollectedHotItem> items = parse(response.body());
            log.info("ThePaper hot board collected: itemCount={}, responseBytes={}, durationMs={}", items.size(), response.body().getBytes(StandardCharsets.UTF_8).length, elapsed(started));
            return items;
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("ThePaper hot board request interrupted", e); }
        catch (IOException e) { log.warn("ThePaper hot board request failed: durationMs={}, error={}", elapsed(started), e.toString()); throw new IllegalStateException("ThePaper hot board request failed", e); }
    }
    List<CollectedHotItem> parse(String json) {
        JsonNode hotNews;
        try { hotNews = objectMapper.readTree(json).path("data").path("hotNews"); }
        catch (JsonProcessingException e) { throw new IllegalStateException("ThePaper hot board data is invalid JSON", e); }
        if (!hotNews.isArray()) throw new IllegalStateException("ThePaper hot board list is missing");
        List<CollectedHotItem> items = new ArrayList<>(); int rank = 1;
        for (JsonNode item : hotNews) {
            String id = text(item, "contId"), title = text(item, "name");
            if (id == null || title == null) continue;
            String detail = "https://www.thepaper.cn/newsDetail_forward_" + id;
            String cover = validUrl(text(item, "pic"));
            LocalDateTime published = item.path("pubTimeLong").canConvertToLong() ? LocalDateTime.ofInstant(Instant.ofEpochMilli(item.path("pubTimeLong").asLong()), ZoneId.of("Asia/Shanghai")) : null;
            items.add(new CollectedHotItem("the-paper-" + id, title, null, source(), categoryClassifier.classify(title, null), 0L, "榜单第 " + rank + " 位", rank++, detail, cover, published));
            if (items.size() >= limit) break;
        }
        if (items.isEmpty()) throw new IllegalStateException("ThePaper hot board contains no usable items");
        return List.copyOf(items);
    }
    private String text(JsonNode n, String f) { String v = n.path(f).asText(null); return v == null || v.isBlank() ? null : v.trim(); }
    private String validUrl(String v) { if (v == null || v.isBlank()) return null; try { URI u = URI.create(v); return ("http".equalsIgnoreCase(u.getScheme()) || "https".equalsIgnoreCase(u.getScheme())) && u.getHost() != null ? u.toString() : null; } catch (IllegalArgumentException e) { return null; } }
    private long elapsed(long s) { return (System.nanoTime() - s) / 1_000_000; }
}
