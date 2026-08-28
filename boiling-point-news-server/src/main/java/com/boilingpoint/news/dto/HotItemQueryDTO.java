package com.boilingpoint.news.dto;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.common.enums.HotTrend;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HotItemQueryDTO {

    private HotSource source;
    private HotCategory category;
    private HotTrend trend;

    @Size(max = 100, message = "关键词长度不能超过100个字符")
    private String keyword;

    @Min(value = 1, message = "返回数量不能小于1")
    @Max(value = 100, message = "返回数量不能超过100")
    private Integer limit = 20;
}
