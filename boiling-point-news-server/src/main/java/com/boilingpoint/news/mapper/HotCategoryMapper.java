package com.boilingpoint.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boilingpoint.news.entity.HotCategoryEntity;

import java.util.List;

public interface HotCategoryMapper extends BaseMapper<HotCategoryEntity> {

    List<HotCategoryEntity> selectEnabledCategories();
}
