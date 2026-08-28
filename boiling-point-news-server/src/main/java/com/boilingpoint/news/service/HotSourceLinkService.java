package com.boilingpoint.news.service;

import com.boilingpoint.news.common.ResultCode;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.entity.HotItemEntity;
import com.boilingpoint.news.exception.BusinessException;
import com.boilingpoint.news.mapper.HotItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class HotSourceLinkService {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36 HoloView/0.1";
    private static final String BAIDU_NEWS_URL = "https://www.baidu.com/s?tn=news&word=";

    private final HotItemMapper hotItemMapper;
    private final HttpClient httpClient;
    private final Duration requestTimeout;
    private final Duration cacheTtl;
    private final Map<Long, CachedLink> cache = new ConcurrentHashMap<>();

    @Autowired
    public HotSourceLinkService(
            HotItemMapper hotItemMapper,
            @Value("${app.source-link.timeout:8s}") Duration requestTimeout,
            @Value("${app.source-link.cache-ttl:30m}") Duration cacheTtl) {
        this(hotItemMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), requestTimeout, cacheTtl);
    }

    HotSourceLinkService(HotItemMapper hotItemMapper, HttpClient httpClient,
                         Duration requestTimeout, Duration cacheTtl) {
        this.hotItemMapper = hotItemMapper;
        this.httpClient = httpClient;
        this.requestTimeout = requestTimeout;
        this.cacheTtl = cacheTtl;
    }

    public URI resolve(Long hotId) {
        if (hotId == null || hotId <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "热点 ID 不合法");
        }
        HotItemEntity item = hotItemMapper.selectById(hotId);
        if (item == null || !Integer.valueOf(1).equals(item.getStatus())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "热点不存在或已下线");
        }
        URI configuredLink = parseHttpUri(item.getSourceUrl());
        if (item.getSource() != HotSource.BAIDU || !isBaiduSearchPage(configuredLink)) {
            if (configuredLink == null) {
                throw new BusinessException("该热点暂未提供可用的报道地址");
            }
            return configuredLink;
        }

        CachedLink cached = cache.get(hotId);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            log.debug("Hot source link cache hit: hotId={}, host={}", hotId, cached.uri().getHost());
            return cached.uri();
        }

        URI resolved = resolveBaiduNewsLink(hotId, item.getTitle());
        cache.put(hotId, new CachedLink(resolved, Instant.now().plus(cacheTtl)));
        return resolved;
    }

    private URI resolveBaiduNewsLink(Long hotId, String title) {
        long startedAt = System.nanoTime();
        URI searchUri = URI.create(BAIDU_NEWS_URL + URLEncoder.encode(title, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(searchUri)
                .timeout(requestTimeout)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BusinessException("报道地址解析暂时不可用");
            }
            URI resolved = parseNewsLink(response.body());
            log.info("Hot source link resolved: hotId={}, provider=BAIDU, targetHost={}, durationMs={}",
                    hotId, resolved.getHost(), elapsedMillis(startedAt));
            return resolved;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException("报道地址解析被中断，请稍后重试");
        } catch (IOException exception) {
            log.warn("Hot source link request failed: hotId={}, provider=BAIDU, durationMs={}, error={}",
                    hotId, elapsedMillis(startedAt), exception.toString());
            throw new BusinessException("报道地址解析失败，请稍后重试");
        }
    }

    URI parseNewsLink(String html) {
        List<URI> candidates = new ArrayList<>();
        for (Element link : Jsoup.parse(html).select("#content_left h3 a[href]")) {
            URI candidate = parseHttpUri(link.attr("href"));
            if (candidate == null || isBaiduSearchPage(candidate)) {
                continue;
            }
            candidates.add(candidate);
        }
        return candidates.stream()
                .filter(candidate -> !isBaiduHost(candidate.getHost()))
                .findFirst()
                .or(() -> candidates.stream().findFirst())
                .orElseThrow(() -> new BusinessException("未找到可用的报道详情地址"));
    }

    private URI parseHttpUri(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(value.trim());
            if (("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null) {
                return uri;
            }
        } catch (IllegalArgumentException ignored) {
            // Invalid external data is skipped and never used as a redirect target.
        }
        return null;
    }

    private boolean isBaiduSearchPage(URI uri) {
        return uri != null && isBaiduHost(uri.getHost()) && "/s".equals(uri.getPath());
    }

    private boolean isBaiduHost(String host) {
        if (host == null) {
            return false;
        }
        String normalized = host.toLowerCase();
        return normalized.equals("baidu.com") || normalized.endsWith(".baidu.com");
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private record CachedLink(URI uri, Instant expiresAt) {
    }
}
