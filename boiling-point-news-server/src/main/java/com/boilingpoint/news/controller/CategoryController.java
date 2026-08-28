package com.boilingpoint.news.controller;

import com.boilingpoint.news.common.Result;
import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.service.CategoryService;
import com.boilingpoint.news.vo.CategoryVO;
import com.boilingpoint.news.vo.HotItemVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.listEnabledCategories());
    }

    @GetMapping("/{category}/hot")
    public Result<List<HotItemVO>> hotItems(
            @PathVariable HotCategory category,
            @RequestParam(required = false)
            @Min(value = 1, message = "返回数量不能小于1")
            @Max(value = 100, message = "返回数量不能超过100") Integer limit) {
        log.debug("Category hot request: category={}, limit={}", category, limit);
        return Result.success(categoryService.getCategoryHotItems(category, limit));
    }
}
