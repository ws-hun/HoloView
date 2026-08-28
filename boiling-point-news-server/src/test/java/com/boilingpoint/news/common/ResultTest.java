package com.boilingpoint.news.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void shouldCreateSuccessfulResult() {
        Result<String> result = Result.success("hot news");

        assertThat(result.code()).isEqualTo(200);
        assertThat(result.message()).isEqualTo("success");
        assertThat(result.data()).isEqualTo("hot news");
    }

    @Test
    void shouldCreateFailureResult() {
        Result<Void> result = Result.failure(ResultCode.BAD_REQUEST, "keyword is required");

        assertThat(result.code()).isEqualTo(400);
        assertThat(result.message()).isEqualTo("keyword is required");
        assertThat(result.data()).isNull();
    }
}
