package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BaiduCategoryClassifier {

    private static final Map<HotCategory, List<String>> RULES = new LinkedHashMap<>();

    static {
        RULES.put(HotCategory.GAMING, List.of("游戏", "电竞", "玩家", "手游"));
        RULES.put(HotCategory.TECHNOLOGY, List.of("人工智能", "大模型", "AI", "芯片", "机器人", "科技", "手机", "数码"));
        RULES.put(HotCategory.AUTOMOTIVE, List.of("汽车", "车企", "新能源车", "驾驶", "4S店", "车展"));
        RULES.put(HotCategory.SPORTS, List.of("比赛", "冠军", "足球", "篮球", "网球", "运动员", "奥运", "世界杯"));
        RULES.put(HotCategory.ENTERTAINMENT, List.of("明星", "演员", "电影", "电视剧", "演唱会", "歌手", "综艺"));
        RULES.put(HotCategory.FINANCE, List.of("股票", "股市", "财报", "银行", "净利", "融资", "价格大涨", "基金"));
        RULES.put(HotCategory.INTERNATIONAL, List.of("国际", "美国", "俄罗斯", "乌克兰", "联合国", "外交", "峰会"));
        RULES.put(HotCategory.LIFESTYLE, List.of("旅游", "旅行", "美食", "饮料", "奶茶", "健康", "穿搭"));
    }

    public HotCategory classify(String title, String description) {
        String text = (title == null ? "" : title) + " " + (description == null ? "" : description);
        for (Map.Entry<HotCategory, List<String>> entry : RULES.entrySet()) {
            if (entry.getValue().stream().anyMatch(text::contains)) {
                return entry.getKey();
            }
        }
        return HotCategory.SOCIETY;
    }
}
