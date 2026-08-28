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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.zhihu.enabled", havingValue = "true", matchIfMissing = true)
public class ZhihuHotSearchCollector implements HotSearchCollector {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 HoloView/0.1";
    private static final Pattern QUESTION_ID = Pattern.compile("/question/(\\d+)");
    private static final Pattern METRICS_NUMBER = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*([万亿千百])?");

    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final HttpClient httpClient;
    private final String url;
    private final int limit;
    private final Duration requestTimeout;

    public ZhihuHotSearchCollector(
            ObjectMapper objectMapper,
            BaiduCategoryClassifier categoryClassifier,
            @Value("${app.collector.zhihu.url:https://www.zhihu.com/api/v3/feed/topstory/hot-list-web?limit=20&desktop=true}") String url,
            @Value("${app.collector.zhihu.limit:20}") int limit,
            @Value("${app.collector.zhihu.timeout:10s}") Duration requestTimeout) {
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
        return HotSource.ZHIHU;
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
                throw new IllegalStateException("Zhihu hot board returned HTTP " + response.statusCode());
            }
            List<CollectedHotItem> items = parse(response.body());
            log.info("Zhihu hot board collected: itemCount={}, responseBytes={}, durationMs={}",
                    items.size(), response.body().getBytes(StandardCharsets.UTF_8).length,
                    elapsedMillis(startedAt));
            return items;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Zhihu hot board request interrupted", exception);
        } catch (IOException exception) {
            log.warn("Zhihu hot board request failed: durationMs={}, error={}",
                    elapsedMillis(startedAt), exception.toString());
            throw new IllegalStateException("Zhihu hot board request failed", exception);
        }
    }

    List<CollectedHotItem> parse(String json) {
        final JsonNode data;
        try {
            data = objectMapper.readTree(json).path("data");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Zhihu hot board data is invalid JSON", exception);
        }
        if (!data.isArray()) {
            throw new IllegalStateException("Zhihu hot board list is missing");
        }

        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        for (JsonNode item : data) {
            JsonNode target = item.path("target");
            String title = text(target.path("title_area"), "text");
            String sourceUrl = text(target.path("link"), "url");
            if (title == null || sourceUrl == null || !isHttpUrl(sourceUrl)) {
                continue;
            }
            String description = text(target.path("excerpt_area"), "text");
            String metrics = text(target.path("metrics_area"), "text");
            long hotValue = parseHotValue(metrics);
            items.add(new CollectedHotItem(
                    stableKey(sourceUrl), title, description, source(),
                    categoryClassifier.classify(title, description), hotValue,
                    metrics == null ? formatHotValue(hotValue) : metrics,
                    rank++, sourceUrl, validHttpUrl(target.path("image_area").path("url").asText(null)), null));
            if (items.size() >= limit) {
                break;
            }
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Zhihu hot board contains no usable items");
        }
        return List.copyOf(items);
    }

    long parseHotValue(String metrics) {
        if (metrics == null || metrics.isBlank()) {
            return 0L;
        }
        Matcher matcher = METRICS_NUMBER.matcher(metrics.replace(",", ""));
        if (!matcher.find()) {
            return 0L;
        }
        try {
            BigDecimal value = new BigDecimal(matcher.group(1));
            String unit = matcher.group(2);
            BigDecimal multiplier = switch (unit == null ? "" : unit) {
                case "亿" -> BigDecimal.valueOf(100_000_000L);
                case "万" -> BigDecimal.valueOf(10_000L);
                case "千" -> BigDecimal.valueOf(1_000L);
                case "百" -> BigDecimal.valueOf(100L);
                default -> BigDecimal.ONE;
            };
            return value.multiply(multiplier).setScale(0, RoundingMode.DOWN).longValue();
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private String stableKey(String sourceUrl) {
        Matcher matcher = QUESTION_ID.matcher(sourceUrl);
        if (matcher.find()) {
            return "zhihu-" + matcher.group(1);
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(sourceUrl.trim().getBytes(StandardCharsets.UTF_8));
            return "zhihu-" + HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isHttpUrl(String value) {
        return validHttpUrl(value) != null;
    }

    private String validHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(value.trim());
            if (("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null) {
                return uri.toString();
            }
        } catch (IllegalArgumentException ignored) {
            // Invalid external data is skipped and never persisted as a link.
        }
        return null;
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
