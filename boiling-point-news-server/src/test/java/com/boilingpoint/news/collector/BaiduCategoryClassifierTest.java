package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaiduCategoryClassifierTest {

    private final BaiduCategoryClassifier classifier = new BaiduCategoryClassifier();

    @Test
    void shouldClassifyKnownTopicsAndFallbackToSociety() {
        assertThat(classifier.classify("新一代芯片发布", null)).isEqualTo(HotCategory.TECHNOLOGY);
        assertThat(classifier.classify("球队赢得冠军", "决赛结束")).isEqualTo(HotCategory.SPORTS);
        assertThat(classifier.classify("普通民生新闻", "暂无明显分类词")).isEqualTo(HotCategory.SOCIETY);
    }
}
