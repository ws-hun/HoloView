package com.boilingpoint.news.collector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;

@Slf4j
abstract class AbstractPublicHotSearchCollector implements HotSearchCollector {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 Chrome/140.0.0.0 Safari/537.36 HoloView/0.1";

    protected final String url;
    protected final int limit;
    private final Duration requestTimeout;
    private final HttpClient httpClient;

    protected AbstractPublicHotSearchCollector(String url, int limit, Duration requestTimeout) {
        this.url = url;
        this.limit = Math.max(1, Math.min(50, limit));
        this.requestTimeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public final List<CollectedHotItem> collect() {
        long startedAt = System.nanoTime();
        RuntimeException lastFailure = null;
        List<String> candidates = candidateUrls();
        for (int index = 0; index < candidates.size(); index++) {
            String candidate = candidates.get(index);
            try {
                HttpResponse<String> response = request(candidate);
                if (response.statusCode() != 200) {
                    throw new IllegalStateException(source() + " public board returned HTTP " + response.statusCode());
                }
                List<CollectedHotItem> items = parse(response.body());
                if (items.isEmpty()) {
                    throw new IllegalStateException(source() + " public board contains no usable items");
                }
                log.info("Public hot board collected: source={}, endpointIndex={}, itemCount={}, responseBytes={}, durationMs={}",
                        source(), index + 1, items.size(), response.body().getBytes(StandardCharsets.UTF_8).length,
                        elapsedMillis(startedAt));
                return List.copyOf(items);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(source() + " public board request interrupted", exception);
            } catch (IOException exception) {
                lastFailure = new IllegalStateException(source() + " public board request failed", exception);
            } catch (RuntimeException exception) {
                lastFailure = exception;
            }
            log.warn("Public hot board endpoint failed: source={}, endpointIndex={}, endpointCount={}, durationMs={}, error={}",
                    source(), index + 1, candidates.size(), elapsedMillis(startedAt), lastFailure.toString());
        }
        throw new IllegalStateException(source() + " public board unavailable after " + candidates.size()
                + " endpoint(s)", lastFailure);
    }

    private HttpResponse<String> request(String candidate) throws IOException, InterruptedException {
        HttpRequest request = customize(HttpRequest.newBuilder(URI.create(candidate)))
                .timeout(requestTimeout)
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    protected List<String> candidateUrls() {
        return List.of(url);
    }

    protected HttpRequest.Builder customize(HttpRequest.Builder builder) {
        return builder.header("Accept", "application/json,text/html,application/xhtml+xml,*/*");
    }

    abstract List<CollectedHotItem> parse(String body);

    protected String text(com.fasterxml.jackson.databind.JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    protected long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    protected String stableKey(String prefix, String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return prefix + java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
