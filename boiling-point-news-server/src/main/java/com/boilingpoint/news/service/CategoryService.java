package com.boilingpoint.news.service;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.vo.CategoryVO;
import com.boilingpoint.news.vo.HotItemVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> listEnabledCategories();

    List<HotItemVO> getCategoryHotItems(HotCategory category, Integer limit);
}
