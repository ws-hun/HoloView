package com.boilingpoint.news.controller;

import com.boilingpoint.news.sse.HotSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

    private final HotSseService hotSseService;

    @GetMapping(value = "/hot", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter hotStream() {
        return hotSseService.connect();
    }
}
