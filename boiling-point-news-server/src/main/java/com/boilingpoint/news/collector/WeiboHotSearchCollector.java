package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.weibo.enabled", havingValue = "true", matchIfMissing = true)
public class WeiboHotSearchCollector extends AbstractPublicHotSearchCollector {

    private static final String BASE_URL = "https://s.weibo.com";
    private static final Pattern NUMBER = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*([万亿千百])?");
    private final ObjectMapper objectMapper;
    private final String fallbackUrl;
    private final String cookie;
    private final BaiduCategoryClassifier categoryClassifier;

    public WeiboHotSearchCollector(ObjectMapper objectMapper, BaiduCategoryClassifier categoryClassifier,
                                   @Value("${app.collector.weibo.url:https://s.weibo.com/top/summary?cate=realtimehot}") String url,
                                   @Value("${app.collector.weibo.fallback-url:}") String fallbackUrl,
                                   @Value("${app.collector.weibo.cookie:}") String cookie,
                                   @Value("${app.collector.weibo.limit:30}") int limit,
                                   @Value("${app.collector.weibo.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
        this.objectMapper = objectMapper;
        this.categoryClassifier = categoryClassifier;
        this.fallbackUrl = fallbackUrl == null ? "" : fallbackUrl.trim();
        this.cookie = cookie == null ? "" : cookie.trim();
    }

    @Override
    public HotSource source() {
        return HotSource.WEIBO;
    }

    @Override
    protected List<String> candidateUrls() {
        return fallbackUrl.isBlank() ? List.of(url) : List.of(url, fallbackUrl);
    }

    @Override
    protected HttpRequest.Builder customize(HttpRequest.Builder builder) {
        HttpRequest.Builder customized = super.customize(builder)
                .header("Referer", BASE_URL + "/top/summary?cate=realtimehot");
        if (!cookie.isBlank()) customized.header("Cookie", cookie);
        return customized;
    }

    @Override
    List<CollectedHotItem> parse(String body) {
        String trimmed = body == null ? "" : body.trim();
        if (trimmed.startsWith("{")) return parseJson(trimmed);
        Document document = Jsoup.parse(trimmed);
        List<CollectedHotItem> items = new ArrayList<>();
        int rank = 1;
        // Weibo has used both a wrapper div and a table id across page revisions.
        for (Element row : document.select("#pl_top_realtimehot table tbody tr, #pl_top_realtimehot tbody tr")) {
            Element link = row.select("td.td-02 a").stream()
                    .filter(item -> !item.attr("href").contains("javascript:void(0)"))
                    .findFirst().orElse(null);
            if (link == null || link.text().isBlank()) continue;
            String title = link.text().trim();
            String href = link.attr("href").trim();
            String hotText = row.select("td.td-02 span, td.td-03").text().trim();
            long hotValue = parseHotValue(hotText);
            items.add(new CollectedHotItem(stableKey("weibo-", title), title, null, source(),
                    categoryClassifier.classify(title, null), hotValue,
                    hotText.isBlank() ? formatHotValue(hotValue) : hotText, rank++,
                    href.startsWith("http") ? href : BASE_URL + href, null, null));
            if (items.size() >= limit) break;
        }
        if (items.isEmpty()) throw new IllegalStateException("Weibo hot board contains no usable items");
        return List.copyOf(items);
    }

    private List<CollectedHotItem> parseJson(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode list = root.path("items").isArray() ? root.path("items") : root.path("data").path("items");
            if (!list.isArray()) throw new IllegalStateException("Weibo fallback payload list is missing");
            List<CollectedHotItem> items = new ArrayList<>();
            int rank = 1;
            for (JsonNode item : list) {
                String title = text(item, "title");
                String sourceUrl = text(item, "url");
                if (title == null || sourceUrl == null) continue;
                long hotValue = parseHotValue(text(item, "hot") == null ? text(item, "hotValue") : text(item, "hot"));
                items.add(new CollectedHotItem(stableKey("weibo-", title), title, text(item, "description"), source(),
                        categoryClassifier.classify(title, text(item, "description")), hotValue, formatHotValue(hotValue), rank++, sourceUrl, null, null));
                if (items.size() >= limit) break;
            }
            if (items.isEmpty()) throw new IllegalStateException("Weibo fallback payload contains no usable items");
            return List.copyOf(items);
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException stateException) throw stateException;
            throw new IllegalStateException("Weibo fallback payload is invalid JSON", exception);
        }
    }

    long parseHotValue(String value) {
        if (value == null || value.isBlank()) return 0;
        Matcher matcher = NUMBER.matcher(value.replace(",", ""));
        if (!matcher.find()) return 0;
        BigDecimal multiplier = switch (matcher.group(2) == null ? "" : matcher.group(2)) {
            case "亿" -> BigDecimal.valueOf(100_000_000L);
            case "万" -> BigDecimal.valueOf(10_000L);
            case "千" -> BigDecimal.valueOf(1_000L);
            case "百" -> BigDecimal.valueOf(100L);
            default -> BigDecimal.ONE;
        };
        return new BigDecimal(matcher.group(1)).multiply(multiplier).longValue();
    }

    private String formatHotValue(long value) {
        if (value >= 100_000_000) return String.format("%.2f亿", value / 100_000_000.0);
        if (value >= 10_000) return String.format("%.1f万", value / 10_000.0);
        return Long.toString(value);
    }
}
