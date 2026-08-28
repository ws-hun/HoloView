package com.boilingpoint.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hot_history")
public class HotHistoryEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long hotId;
    private Long hotValue;
    @TableField("`rank`")
    private Integer rank;
    private LocalDateTime recordedAt;
}
