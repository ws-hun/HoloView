package com.boilingpoint.news.controller;

import com.boilingpoint.news.common.Result;
import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.dto.HotSearchQueryDTO;
import com.boilingpoint.news.dto.HotTrendQueryDTO;
import com.boilingpoint.news.service.HotQueryService;
import com.boilingpoint.news.service.HotSourceLinkService;
import com.boilingpoint.news.vo.HotDetailVO;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.HotTrendPointVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hot")
public class HotController {

    private final HotQueryService hotQueryService;
    private final HotSourceLinkService hotSourceLinkService;

    @GetMapping("/list")
    public Result<List<HotItemVO>> list(@Valid HotItemQueryDTO query) {
        log.debug("Hot list request: source={}, category={}, trend={}, keywordPresent={}, limit={}",
                query.getSource(), query.getCategory(), query.getTrend(),
                query.getKeyword() != null && !query.getKeyword().isBlank(), query.getLimit());
        return Result.success(hotQueryService.list(query));
    }

    @GetMapping("/{id}")
    public Result<HotDetailVO> detail(@PathVariable Long id) {
        log.debug("Hot detail request: hotId={}", id);
        return Result.success(hotQueryService.getDetail(id));
    }

    @GetMapping("/{id}/source")
    public ResponseEntity<Void> source(@PathVariable Long id) {
        URI location = hotSourceLinkService.resolve(id);
        log.info("Hot source redirect: hotId={}, targetHost={}", id, location.getHost());
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("/ranking")
    public Result<List<HotItemVO>> ranking(@RequestParam(required = false)
                                           @Min(value = 1, message = "返回数量不能小于1")
                                           @Max(value = 100, message = "返回数量不能超过100")
                                           Integer limit) {
        return Result.success(hotQueryService.ranking(limit));
    }

    @GetMapping("/trending")
    public Result<List<HotItemVO>> trending(@RequestParam(required = false)
                                            @Min(value = 1, message = "返回数量不能小于1")
                                            @Max(value = 100, message = "返回数量不能超过100")
                                            Integer limit) {
        return Result.success(hotQueryService.trending(limit));
    }

    @GetMapping("/latest")
    public Result<List<HotItemVO>> latest(@RequestParam(required = false)
                                          @Min(value = 1, message = "返回数量不能小于1")
                                          @Max(value = 100, message = "返回数量不能超过100")
                                          Integer limit) {
        return Result.success(hotQueryService.latest(limit));
    }

    @GetMapping("/search")
    public Result<List<HotItemVO>> search(@Valid HotSearchQueryDTO query) {
        log.debug("Hot search request: keywordLength={}, limit={}",
                query.getKeyword() == null ? 0 : query.getKeyword().length(), query.getLimit());
        return Result.success(hotQueryService.search(query));
    }

    @GetMapping("/{id}/trend")
    public Result<List<HotTrendPointVO>> trend(@PathVariable Long id,
                                               @Valid HotTrendQueryDTO query) {
        log.debug("Hot trend request: hotId={}, hours={}, limit={}",
                id, query.getHours(), query.getLimit());
        return Result.success(hotQueryService.getTrendPoints(id, query));
    }
}
