package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
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
@ConditionalOnProperty(name = "app.collector.baidu.enabled", havingValue = "true", matchIfMissing = true)
public class BaiduHotSearchCollector implements HotSearchCollector {

    private static final String DATA_PREFIX = "s-data:";
    private static final String USER_AGENT = "HoloView/0.1 (+https://github.com/ws-hun/HoloView)";

    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final HttpClient httpClient;
    private final String url;
    private final int limit;
    private final Duration requestTimeout;

    public BaiduHotSearchCollector(
            ObjectMapper objectMapper,
            BaiduCategoryClassifier categoryClassifier,
            @Value("${app.collector.baidu.url:https://top.baidu.com/board?tab=realtime}") String url,
            @Value("${app.collector.baidu.limit:30}") int limit,
            @Value("${app.collector.baidu.timeout:10s}") Duration requestTimeout) {
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
        return HotSource.BAIDU;
    }

    @Override
    public List<CollectedHotItem> collect() {
        long startedAt = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(requestTimeout)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Baidu hot board returned HTTP " + response.statusCode());
            }
            List<CollectedHotItem> items = parse(response.body());
            log.info("Baidu hot board collected: itemCount={}, responseBytes={}, durationMs={}",
                    items.size(), response.body().getBytes(StandardCharsets.UTF_8).length,
                    elapsedMillis(startedAt));
            return items;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Baidu hot board request interrupted", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Baidu hot board request failed", exception);
        }
    }

    List<CollectedHotItem> parse(String html) {
        JsonNode content = findHotListContent(extractPageData(Jsoup.parse(html)));
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : content) {
            String title = text(item, "word");
            if (title == null || title.isBlank()) {
                continue;
            }
            String description = text(item, "desc");
            long hotScore = positiveLong(item.path("hotScore").asText());
            items.add(new CollectedHotItem(
                    stableKey(title), title, description, source(),
                    categoryClassifier.classify(title, description), hotScore,
                    formatHotValue(hotScore), rank++, text(item, "url"),
                    text(item, "img"), null));
            if (items.size() >= limit) {
                break;
            }
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Baidu hot board contains no usable items");
        }
        return List.copyOf(items);
    }

    private JsonNode extractPageData(Document document) {
        Element root = document.getElementById("sanRoot");
        if (root == null) {
            throw new IllegalStateException("Baidu hot board root element is missing");
        }
        for (Node node : root.childNodes()) {
            if (node instanceof Comment comment && comment.getData().startsWith(DATA_PREFIX)) {
                try {
                    return objectMapper.readTree(comment.getData().substring(DATA_PREFIX.length()));
                } catch (JsonProcessingException exception) {
                    throw new IllegalStateException("Baidu hot board data is invalid JSON", exception);
                }
            }
        }
        throw new IllegalStateException("Baidu hot board page data is missing");
    }

    private JsonNode findHotListContent(JsonNode pageData) {
        for (JsonNode card : pageData.path("data").path("cards")) {
            if ("hotList".equals(card.path("component").asText()) && card.path("content").isArray()) {
                return card.path("content");
            }
        }
        throw new IllegalStateException("Baidu hot board list is missing");
    }

    private String stableKey(String title) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(title.trim().getBytes(StandardCharsets.UTF_8));
            return "baidu-" + HexFormat.of().formatHex(digest, 0, 16);
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
