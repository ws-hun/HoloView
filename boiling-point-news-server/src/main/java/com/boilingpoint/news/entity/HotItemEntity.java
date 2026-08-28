package com.boilingpoint.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.common.enums.HotTrend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hot_item")
public class HotItemEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;
    private String description;
    private HotSource source;
    private String sourceItemKey;
    private String sourceUrl;
    private HotCategory category;
    private Long hotValue;
    private String hotValueText;
    @TableField("`rank`")
    private Integer rank;
    private Integer previousRank;
    private Integer rankChange;
    private HotTrend trend;
    private String cover;
    private LocalDateTime publishedAt;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
