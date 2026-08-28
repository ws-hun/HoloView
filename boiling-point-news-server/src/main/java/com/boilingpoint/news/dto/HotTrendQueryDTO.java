package com.boilingpoint.news.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class HotTrendQueryDTO {

    @Min(value = 1, message = "趋势时间范围不能小于1小时")
    @Max(value = 168, message = "趋势时间范围不能超过168小时")
    private Integer hours = 24;

    @Min(value = 1, message = "返回数量不能小于1")
    @Max(value = 100, message = "返回数量不能超过100")
    private Integer limit = 20;
}
