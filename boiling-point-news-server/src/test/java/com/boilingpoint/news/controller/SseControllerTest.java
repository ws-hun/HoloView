package com.boilingpoint.news.controller;

import com.boilingpoint.news.sse.HotSseService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SseControllerTest {

    @Test
    void shouldOpenAsyncEventStream() throws Exception {
        HotSseService service = mock(HotSseService.class);
        when(service.connect()).thenReturn(new SseEmitter(60_000L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SseController(service)).build();

        mockMvc.perform(get("/api/sse/hot"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }
}
