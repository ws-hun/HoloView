package com.boilingpoint.news.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HotQueryDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectBlankSearchKeyword() {
        HotSearchQueryDTO query = new HotSearchQueryDTO();
        query.setKeyword("  ");

        Set<ConstraintViolation<HotSearchQueryDTO>> violations = validator.validate(query);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("搜索关键词不能为空");
    }

    @Test
    void shouldRejectExcessiveListLimit() {
        HotItemQueryDTO query = new HotItemQueryDTO();
        query.setLimit(101);

        Set<ConstraintViolation<HotItemQueryDTO>> violations = validator.validate(query);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("返回数量不能超过100");
    }
}
