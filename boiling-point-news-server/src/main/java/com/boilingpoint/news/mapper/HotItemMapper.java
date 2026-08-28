package com.boilingpoint.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.entity.HotItemEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HotItemMapper extends BaseMapper<HotItemEntity> {

    List<HotItemEntity> selectHotItems(@Param("query") HotItemQueryDTO query);

    List<HotItemEntity> selectLatestItems(@Param("limit") int limit);

    List<HotItemEntity> selectRelatedItems(
            @Param("category") HotCategory category,
            @Param("excludeId") Long excludeId,
            @Param("limit") int limit
    );

    HotItemEntity selectBySourceAndKey(@Param("source") com.boilingpoint.news.common.enums.HotSource source,
                                       @Param("sourceItemKey") String sourceItemKey);
}
