package com.boilingpoint.news.controller;

import com.boilingpoint.news.common.Result;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.service.PlatformService;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.PlatformVO;
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
@RequestMapping("/api/platform")
public class PlatformController {

    private final PlatformService platformService;

    @GetMapping("/list")
    public Result<List<PlatformVO>> list() {
        return Result.success(platformService.listEnabledPlatforms());
    }

    @GetMapping("/{source}/hot")
    public Result<List<HotItemVO>> hotItems(
            @PathVariable HotSource source,
            @RequestParam(required = false)
            @Min(value = 1, message = "返回数量不能小于1")
            @Max(value = 100, message = "返回数量不能超过100") Integer limit) {
        log.debug("Platform hot request: source={}, limit={}", source, limit);
        return Result.success(platformService.getPlatformHotItems(source, limit));
    }
}
