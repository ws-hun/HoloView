package com.boilingpoint.news.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HotSearchQueryDTO {

    @NotBlank(message = "搜索关键词不能为空")
    @Size(max = 100, message = "搜索关键词长度不能超过100个字符")
    private String keyword;

    @Min(value = 1, message = "返回数量不能小于1")
    @Max(value = 100, message = "返回数量不能超过100")
    private Integer limit = 20;
}
