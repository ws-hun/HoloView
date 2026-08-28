package com.boilingpoint.news.service.impl;

import com.boilingpoint.news.common.ResultCode;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.converter.DictionaryConverter;
import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.exception.BusinessException;
import com.boilingpoint.news.mapper.SourcePlatformMapper;
import com.boilingpoint.news.service.HotQueryService;
import com.boilingpoint.news.service.PlatformService;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.PlatformVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformServiceImpl implements PlatformService {

    private final SourcePlatformMapper sourcePlatformMapper;
    private final DictionaryConverter dictionaryConverter;
    private final HotQueryService hotQueryService;

    @Override
    public List<PlatformVO> listEnabledPlatforms() {
        long startedAt = System.nanoTime();
        List<PlatformVO> result = dictionaryConverter.toPlatformVOList(
                sourcePlatformMapper.selectEnabledPlatforms());
        log.debug("Enabled platforms queried: resultCount={}, durationMs={}",
                result.size(), (System.nanoTime() - startedAt) / 1_000_000);
        return result;
    }

    @Override
    public List<HotItemVO> getPlatformHotItems(HotSource source, Integer limit) {
        if (source == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "平台代码不能为空");
        }
        HotItemQueryDTO query = new HotItemQueryDTO();
        query.setSource(source);
        query.setLimit(limit);
        List<HotItemVO> result = hotQueryService.list(query);
        log.debug("Platform hot items queried: source={}, resultCount={}", source, result.size());
        return result;
    }
}
