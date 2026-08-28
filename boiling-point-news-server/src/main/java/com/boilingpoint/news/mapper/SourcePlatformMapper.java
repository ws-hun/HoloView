package com.boilingpoint.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boilingpoint.news.entity.SourcePlatformEntity;

import java.util.List;

public interface SourcePlatformMapper extends BaseMapper<SourcePlatformEntity> {

    List<SourcePlatformEntity> selectEnabledPlatforms();
}
