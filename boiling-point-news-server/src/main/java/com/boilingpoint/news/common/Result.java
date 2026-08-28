package com.boilingpoint.news.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record Result<T>(int code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return of(ResultCode.SUCCESS, data);
    }

    public static Result<Void> success() {
        return of(ResultCode.SUCCESS, null);
    }

    public static Result<Void> failure(ResultCode resultCode) {
        return of(resultCode, null);
    }

    public static Result<Void> failure(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    public static Result<Void> failure(int code, String message) {
        return new Result<>(code, message, null);
    }

    private static <T> Result<T> of(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }
}
