package com.boilingpoint.news.sse;

import com.boilingpoint.news.event.HotCollectionCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class HotSseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final long timeout;

    public HotSseService(@Value("${app.sse.timeout:1800000}") long timeout) {
        this.timeout = timeout;
    }

    public SseEmitter connect() {
        String connectionId = UUID.randomUUID().toString().replace("-", "");
        SseEmitter emitter = new SseEmitter(timeout);
        emitters.put(connectionId, emitter);
        emitter.onCompletion(() -> remove(connectionId, "completed"));
        emitter.onTimeout(() -> remove(connectionId, "timeout"));
        emitter.onError(error -> remove(connectionId, "error:" + error.getClass().getSimpleName()));
        try {
            emitter.send(SseEmitter.event()
                    .id(connectionId)
                    .name("connected")
                    .data(Map.of("connectionId", connectionId, "connectedAt", LocalDateTime.now())));
            log.info("SSE client connected: connectionId={}, activeConnections={}", connectionId, emitters.size());
        } catch (IOException exception) {
            emitters.remove(connectionId);
            emitter.completeWithError(exception);
            log.warn("SSE initial event failed: connectionId={}, error={}", connectionId, exception.toString());
        }
        return emitter;
    }

    public void broadcastHotUpdate(HotCollectionCompletedEvent event) {
        broadcast("hot-update", event);
    }

    @Scheduled(initialDelayString = "${app.sse.heartbeat-interval:30000}",
            fixedRateString = "${app.sse.heartbeat-interval:30000}")
    public void heartbeat() {
        if (!emitters.isEmpty()) {
            broadcast("heartbeat", Map.of("sentAt", LocalDateTime.now()));
        }
    }

    public int connectionCount() {
        return emitters.size();
    }

    private void broadcast(String eventName, Object data) {
        emitters.forEach((connectionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException | IllegalStateException exception) {
                emitters.remove(connectionId);
                log.warn("SSE send failed: connectionId={}, event={}, error={}",
                        connectionId, eventName, exception.toString());
            }
        });
        log.debug("SSE event broadcast: event={}, activeConnections={}", eventName, emitters.size());
    }

    private void remove(String connectionId, String reason) {
        if (emitters.remove(connectionId) != null) {
            log.info("SSE client removed: connectionId={}, reason={}, activeConnections={}",
                    connectionId, reason, emitters.size());
        }
    }
}
