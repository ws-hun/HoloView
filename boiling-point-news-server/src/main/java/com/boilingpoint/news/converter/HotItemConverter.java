package com.boilingpoint.news.converter;

import com.boilingpoint.news.entity.HotHistoryEntity;
import com.boilingpoint.news.entity.HotItemEntity;
import com.boilingpoint.news.vo.HotDetailVO;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.HotTrendPointVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class HotItemConverter {

    public HotItemVO toVO(HotItemEntity entity) {
        if (entity == null) {
            return null;
        }

        return new HotItemVO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSource(),
                entity.getSource() == null ? null : entity.getSource().getLabel(),
                entity.getSourceUrl(),
                entity.getCategory(),
                entity.getCategory() == null ? null : entity.getCategory().getLabel(),
                entity.getHotValue(),
                entity.getHotValueText(),
                entity.getRank(),
                entity.getPreviousRank(),
                entity.getRankChange(),
                entity.getTrend(),
                entity.getCover(),
                entity.getPublishedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<HotItemVO> toVOList(List<HotItemEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toVO)
                .toList();
    }

    public HotTrendPointVO toTrendPointVO(HotHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new HotTrendPointVO(entity.getHotValue(), entity.getRank(), entity.getRecordedAt());
    }

    public List<HotTrendPointVO> toTrendPointVOList(List<HotHistoryEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toTrendPointVO)
                .toList();
    }

    public HotDetailVO toDetailVO(
            HotItemEntity item,
            List<HotHistoryEntity> history,
            List<HotItemEntity> relatedItems
    ) {
        if (item == null) {
            return null;
        }
        return new HotDetailVO(toVO(item), toTrendPointVOList(history), toVOList(relatedItems));
    }
}
