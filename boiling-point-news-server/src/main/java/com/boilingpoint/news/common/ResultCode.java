package com.boilingpoint.news.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(422, "业务处理失败"),
    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;
}
