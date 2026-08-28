package com.boilingpoint.news.sse;

import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.event.HotCollectionCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class HotSseServiceTest {

    @Test
    void shouldRegisterConnectionAndBroadcastEvents() {
        HotSseService service = new HotSseService(60_000);

        SseEmitter emitter = service.connect();
        service.broadcastHotUpdate(new HotCollectionCompletedEvent(HotSource.WEIBO, 3, LocalDateTime.now()));
        service.heartbeat();

        assertThat(emitter).isNotNull();
        assertThat(service.connectionCount()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRemoveDisconnectedEmitterWithoutCompletingAgain() throws Exception {
        HotSseService service = new HotSseService(60_000);
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException("client disconnected")).when(emitter).send(org.mockito.ArgumentMatchers.any(SseEmitter.SseEventBuilder.class));
        Field field = HotSseService.class.getDeclaredField("emitters");
        field.setAccessible(true);
        ((Map<String, SseEmitter>) field.get(service)).put("connection-1", emitter);

        service.broadcastHotUpdate(new HotCollectionCompletedEvent(HotSource.WEIBO, 1, LocalDateTime.now()));

        assertThat(service.connectionCount()).isZero();
        org.mockito.Mockito.verify(emitter, org.mockito.Mockito.never()).complete();
    }
}
