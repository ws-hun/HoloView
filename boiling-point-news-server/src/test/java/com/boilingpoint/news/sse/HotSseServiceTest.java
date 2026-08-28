package com.boilingpoint.news.sse;

import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.event.HotCollectionCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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
}
