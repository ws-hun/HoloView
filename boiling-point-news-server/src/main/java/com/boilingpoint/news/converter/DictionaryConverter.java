package com.boilingpoint.news.converter;

import com.boilingpoint.news.entity.HotCategoryEntity;
import com.boilingpoint.news.entity.SourcePlatformEntity;
import com.boilingpoint.news.vo.CategoryVO;
import com.boilingpoint.news.vo.PlatformVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class DictionaryConverter {

    public PlatformVO toPlatformVO(SourcePlatformEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PlatformVO(entity.getId(), entity.getName(), entity.getCode(), entity.getLogo(), entity.getSort());
    }

    public List<PlatformVO> toPlatformVOList(List<SourcePlatformEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toPlatformVO)
                .toList();
    }

    public CategoryVO toCategoryVO(HotCategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CategoryVO(entity.getId(), entity.getName(), entity.getCode(), entity.getSort());
    }

    public List<CategoryVO> toCategoryVOList(List<HotCategoryEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toCategoryVO)
                .toList();
    }
}
