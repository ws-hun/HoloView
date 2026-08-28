package com.boilingpoint.news.service.impl;

import com.boilingpoint.news.common.ResultCode;
import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.converter.DictionaryConverter;
import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.exception.BusinessException;
import com.boilingpoint.news.mapper.HotCategoryMapper;
import com.boilingpoint.news.service.CategoryService;
import com.boilingpoint.news.service.HotQueryService;
import com.boilingpoint.news.vo.CategoryVO;
import com.boilingpoint.news.vo.HotItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final HotCategoryMapper hotCategoryMapper;
    private final DictionaryConverter dictionaryConverter;
    private final HotQueryService hotQueryService;

    @Override
    public List<CategoryVO> listEnabledCategories() {
        long startedAt = System.nanoTime();
        List<CategoryVO> result = dictionaryConverter.toCategoryVOList(
                hotCategoryMapper.selectEnabledCategories());
        log.debug("Enabled categories queried: resultCount={}, durationMs={}",
                result.size(), (System.nanoTime() - startedAt) / 1_000_000);
        return result;
    }

    @Override
    public List<HotItemVO> getCategoryHotItems(HotCategory category, Integer limit) {
        if (category == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "分类代码不能为空");
        }
        HotItemQueryDTO query = new HotItemQueryDTO();
        query.setCategory(category);
        query.setLimit(limit);
        List<HotItemVO> result = hotQueryService.list(query);
        log.debug("Category hot items queried: category={}, resultCount={}", category, result.size());
        return result;
    }
}
