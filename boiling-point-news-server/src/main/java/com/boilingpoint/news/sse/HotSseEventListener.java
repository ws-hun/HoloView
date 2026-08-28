package com.boilingpoint.news.sse;

import com.boilingpoint.news.event.HotCollectionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HotSseEventListener {

    private final HotSseService hotSseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCollectionCompleted(HotCollectionCompletedEvent event) {
        hotSseService.broadcastHotUpdate(event);
    }
}
