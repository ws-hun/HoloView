package com.boilingpoint.news.service.impl;

import com.boilingpoint.news.common.ResultCode;
import com.boilingpoint.news.common.enums.HotTrend;
import com.boilingpoint.news.converter.HotItemConverter;
import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.dto.HotSearchQueryDTO;
import com.boilingpoint.news.dto.HotTrendQueryDTO;
import com.boilingpoint.news.entity.HotHistoryEntity;
import com.boilingpoint.news.entity.HotItemEntity;
import com.boilingpoint.news.exception.BusinessException;
import com.boilingpoint.news.mapper.HotHistoryMapper;
import com.boilingpoint.news.mapper.HotItemMapper;
import com.boilingpoint.news.service.HotQueryService;
import com.boilingpoint.news.service.HotCacheService;
import com.boilingpoint.news.vo.HotDetailVO;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.HotTrendPointVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class HotQueryServiceImpl implements HotQueryService {

    private static final int DEFAULT_LIST_LIMIT = 20;
    private static final int DEFAULT_TREND_HOURS = 24;
    private static final int DEFAULT_TREND_POINT_LIMIT = 100;
    private static final int RELATED_ITEM_LIMIT = 4;
    private static final Duration LIST_CACHE_TTL = Duration.ofSeconds(30);
    private static final Duration DETAIL_CACHE_TTL = Duration.ofMinutes(2);
    private static final Duration TREND_CACHE_TTL = Duration.ofMinutes(1);

    private final HotItemMapper hotItemMapper;
    private final HotHistoryMapper hotHistoryMapper;
    private final HotItemConverter hotItemConverter;
    private final HotCacheService hotCacheService;

    public HotQueryServiceImpl(HotItemMapper hotItemMapper, HotHistoryMapper hotHistoryMapper,
                               HotItemConverter hotItemConverter) {
        this(hotItemMapper, hotHistoryMapper, hotItemConverter, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public HotQueryServiceImpl(HotItemMapper hotItemMapper, HotHistoryMapper hotHistoryMapper,
                               HotItemConverter hotItemConverter, HotCacheService hotCacheService) {
        this.hotItemMapper = hotItemMapper;
        this.hotHistoryMapper = hotHistoryMapper;
        this.hotItemConverter = hotItemConverter;
        this.hotCacheService = hotCacheService;
    }

    @Override
    public List<HotItemVO> list(HotItemQueryDTO query) {
        long startedAt = System.nanoTime();
        HotItemQueryDTO normalized = normalizeQuery(query);
        String cacheKey = listCacheKey(normalized);
        if (hotCacheService != null) {
            List<HotItemVO> cached = hotCacheService.get(cacheKey);
            if (cached != null) {
                log.debug("Hot list cache hit: key={}, resultCount={}", cacheKey, cached.size());
                return cached;
            }
        }
        List<HotItemVO> result = hotItemConverter.toVOList(hotItemMapper.selectHotItems(normalized));
        if (hotCacheService != null) {
            hotCacheService.put(cacheKey, result, LIST_CACHE_TTL);
        }
        log.debug("Hot list queried: source={}, category={}, trend={}, keywordPresent={}, limit={}, resultCount={}, durationMs={}",
                normalized.getSource(), normalized.getCategory(), normalized.getTrend(),
                normalized.getKeyword() != null, normalized.getLimit(), result.size(), elapsedMillis(startedAt));
        return result;
    }

    @Override
    public HotDetailVO getDetail(Long id) {
        requirePositiveId(id);
        long startedAt = System.nanoTime();
        String cacheKey = "hot:detail:" + id;
        if (hotCacheService != null) {
            HotDetailVO cached = hotCacheService.get(cacheKey);
            if (cached != null) {
                log.debug("Hot detail cache hit: hotId={}", id);
                return cached;
            }
        }
        HotItemEntity item = hotItemMapper.selectById(id);
        if (item == null || !Integer.valueOf(1).equals(item.getStatus())) {
            log.warn("Hot detail not found or inactive: hotId={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "热点不存在或已下线");
        }

        LocalDateTime since = LocalDateTime.now().minusHours(DEFAULT_TREND_HOURS);
        List<HotHistoryEntity> history = hotHistoryMapper.selectTrendPoints(
                id, since, DEFAULT_TREND_POINT_LIMIT);
        List<HotItemEntity> relatedItems = hotItemMapper.selectRelatedItems(
                item.getCategory(), id, RELATED_ITEM_LIMIT);
        HotDetailVO detail = hotItemConverter.toDetailVO(item, history, relatedItems);
        if (hotCacheService != null) {
            hotCacheService.put(cacheKey, detail, DETAIL_CACHE_TTL);
        }
        log.debug("Hot detail queried: hotId={}, trendPointCount={}, relatedCount={}, durationMs={}",
                id, history.size(), relatedItems.size(), elapsedMillis(startedAt));
        return detail;
    }

    @Override
    public List<HotItemVO> ranking(Integer limit) {
        HotItemQueryDTO query = new HotItemQueryDTO();
        query.setLimit(normalizeLimit(limit, DEFAULT_LIST_LIMIT));
        return list(query);
    }

    @Override
    public List<HotItemVO> trending(Integer limit) {
        HotItemQueryDTO query = new HotItemQueryDTO();
        query.setTrend(HotTrend.UP);
        query.setLimit(normalizeLimit(limit, DEFAULT_LIST_LIMIT));
        return list(query);
    }

    @Override
    public List<HotItemVO> latest(Integer limit) {
        long startedAt = System.nanoTime();
        int normalizedLimit = normalizeLimit(limit, DEFAULT_LIST_LIMIT);
        String cacheKey = "hot:latest:" + normalizedLimit;
        if (hotCacheService != null) {
            List<HotItemVO> cached = hotCacheService.get(cacheKey);
            if (cached != null) {
                log.debug("Latest hot cache hit: key={}, resultCount={}", cacheKey, cached.size());
                return cached;
            }
        }
        List<HotItemVO> result = hotItemConverter.toVOList(
                hotItemMapper.selectLatestItems(normalizedLimit));
        if (hotCacheService != null) {
            hotCacheService.put(cacheKey, result, LIST_CACHE_TTL);
        }
        log.debug("Latest hot items queried: limit={}, resultCount={}, durationMs={}",
                normalizedLimit, result.size(), elapsedMillis(startedAt));
        return result;
    }

    @Override
    public List<HotItemVO> search(HotSearchQueryDTO query) {
        if (query == null || query.getKeyword() == null || query.getKeyword().isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "搜索关键词不能为空");
        }
        HotItemQueryDTO listQuery = new HotItemQueryDTO();
        listQuery.setKeyword(query.getKeyword().trim());
        listQuery.setLimit(normalizeLimit(query.getLimit(), DEFAULT_LIST_LIMIT));
        return list(listQuery);
    }

    @Override
    public List<HotTrendPointVO> getTrendPoints(Long hotId, HotTrendQueryDTO query) {
        requirePositiveId(hotId);
        int hours = query == null || query.getHours() == null
                ? DEFAULT_TREND_HOURS : Math.max(1, Math.min(168, query.getHours()));
        int limit = query == null
                ? DEFAULT_TREND_POINT_LIMIT : normalizeLimit(query.getLimit(), DEFAULT_TREND_POINT_LIMIT);
        long startedAt = System.nanoTime();
        String cacheKey = "hot:trend:" + hotId + ":" + hours + ":" + limit;
        if (hotCacheService != null) {
            List<HotTrendPointVO> cached = hotCacheService.get(cacheKey);
            if (cached != null) {
                log.debug("Hot trend cache hit: key={}, resultCount={}", cacheKey, cached.size());
                return cached;
            }
        }
        List<HotTrendPointVO> result = hotItemConverter.toTrendPointVOList(
                hotHistoryMapper.selectTrendPoints(hotId, LocalDateTime.now().minusHours(hours), limit));
        if (hotCacheService != null) {
            hotCacheService.put(cacheKey, result, TREND_CACHE_TTL);
        }
        log.debug("Hot trend queried: hotId={}, hours={}, limit={}, resultCount={}, durationMs={}",
                hotId, hours, limit, result.size(), elapsedMillis(startedAt));
        return result;
    }

    private HotItemQueryDTO normalizeQuery(HotItemQueryDTO query) {
        HotItemQueryDTO normalized = new HotItemQueryDTO();
        if (query != null) {
            normalized.setSource(query.getSource());
            normalized.setCategory(query.getCategory());
            normalized.setTrend(query.getTrend());
            normalized.setKeyword(query.getKeyword());
            normalized.setLimit(query.getLimit());
        }
        normalized.setLimit(normalizeLimit(normalized.getLimit(), DEFAULT_LIST_LIMIT));
        if (normalized.getKeyword() != null) {
            String keyword = normalized.getKeyword().trim();
            normalized.setKeyword(keyword.isEmpty() ? null : keyword);
        }
        return normalized;
    }

    private int normalizeLimit(Integer limit, int defaultValue) {
        if (limit == null) {
            return defaultValue;
        }
        return Math.max(1, Math.min(100, limit));
    }

    private String listCacheKey(HotItemQueryDTO query) {
        return "hot:list:" + value(query.getSource()) + ":" + value(query.getCategory()) + ":"
                + value(query.getTrend()) + ":" + (query.getKeyword() == null ? "" : query.getKeyword())
                + ":" + query.getLimit();
    }

    private String value(Object value) {
        return value == null ? "-" : value.toString();
    }

    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "热点ID必须为正整数");
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
