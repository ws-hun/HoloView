package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.douyin.enabled", havingValue = "true", matchIfMissing = true)
public class DouyinHotSearchCollector extends AbstractPublicHotSearchCollector {

    private static final Pattern HOT_NUMBER = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*([万亿千百])?");

    private final ObjectMapper objectMapper;
    private final BaiduCategoryClassifier categoryClassifier;
    private final String fallbackUrl;

    public DouyinHotSearchCollector(ObjectMapper objectMapper, BaiduCategoryClassifier categoryClassifier,
                                    @Value("${app.collector.douyin.url:https://www.douyin.com/aweme/v1/web/hot/search/list/?device_platform=webapp&aid=6383&channel=channel_pc_web&detail_list=1}") String url,
                                    @Value("${app.collector.douyin.fallback-url:}") String fallbackUrl,
                                    @Value("${app.collector.douyin.limit:30}") int limit,
                                    @Value("${app.collector.douyin.timeout:10s}") Duration timeout) {
        super(url, limit, timeout);
        this.objectMapper = objectMapper;
        this.categoryClassifier = categoryClassifier;
        this.fallbackUrl = fallbackUrl == null ? "" : fallbackUrl.trim();
    }

    @Override
    public HotSource source() {
        return HotSource.DOUYIN;
    }

    @Override
    protected List<String> candidateUrls() {
        return fallbackUrl.isBlank() ? List.of(url) : List.of(url, fallbackUrl);
    }

    @Override
    List<CollectedHotItem> parse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode list = root.path("data").path("word_list");
            if (!list.isArray()) list = root.path("items");
            if (!list.isArray()) throw new IllegalStateException("Douyin hot board list is missing");
            List<CollectedHotItem> items = new ArrayList<>();
            int rank = 1;
            for (JsonNode item : list) {
                String title = text(item, "word");
                if (title == null) title = text(item, "title");
                String id = text(item, "sentence_id");
                if (id == null) id = text(item, "id");
                if (title == null || id == null) continue;
                JsonNode hotNode = item.has("hot_value") ? item.path("hot_value") : item.path("hotValue");
                long hotValue = parseHotValue(hotNode);
                String sourceUrl = text(item, "url");
                if (sourceUrl == null) sourceUrl = "https://www.douyin.com/hot/" + id;
                items.add(new CollectedHotItem("douyin-" + id, title, null, source(),
                        categoryClassifier.classify(title, null), hotValue,
                        hotValue > 0 ? formatHotValue(hotValue) : "热点", rank++, sourceUrl, null, null));
                if (items.size() >= limit) break;
            }
            if (items.isEmpty()) throw new IllegalStateException("Douyin hot board contains no usable items");
            return List.copyOf(items);
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException stateException) throw stateException;
            throw new IllegalStateException("Douyin hot board data is invalid JSON", exception);
        }
    }

    long parseHotValue(JsonNode value) {
        if (value == null || value.isMissingNode() || value.isNull()) return 0L;
        if (value.isNumber()) return value.asLong(0L);
        return parseHotValue(value.asText(null));
    }

    long parseHotValue(String value) {
        if (value == null || value.isBlank()) return 0L;
        Matcher matcher = HOT_NUMBER.matcher(value.replace(",", ""));
        if (!matcher.find()) return 0L;
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
