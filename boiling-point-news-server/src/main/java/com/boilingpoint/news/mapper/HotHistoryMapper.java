package com.boilingpoint.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boilingpoint.news.entity.HotHistoryEntity;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HotHistoryMapper extends BaseMapper<HotHistoryEntity> {

    List<HotHistoryEntity> selectTrendPoints(
            @Param("hotId") Long hotId,
            @Param("since") LocalDateTime since,
            @Param("limit") int limit
    );
}
