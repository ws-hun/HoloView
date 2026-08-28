package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotCategory;
import com.boilingpoint.news.common.enums.HotSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev", "test", "local"})
public class MockHotSearchCollector implements HotSearchCollector {

    @Override
    public HotSource source() {
        return HotSource.OTHER;
    }

    @Override
    public List<CollectedHotItem> collect() {
        long offset = (System.currentTimeMillis() / 60_000) % 10;
        return List.of(
                new CollectedHotItem("mock-1", "城市公共服务升级进入新阶段", "多地发布便民服务新举措",
                        source(), HotCategory.SOCIETY, 72_000_000L + offset * 120_000L, "" + (7200 + offset * 12) + "万",
                        1, "https://example.com/mock-1", null, null),
                new CollectedHotItem("mock-2", "国产大模型应用加速落地", "产业应用从试点走向规模化",
                        source(), HotCategory.TECHNOLOGY, 68_000_000L + offset * 80_000L, "" + (6800 + offset * 8) + "万",
                        2, "https://example.com/mock-2", null, null),
                new CollectedHotItem("mock-3", "新能源汽车补能网络持续完善", "充换电基础设施建设提速",
                        source(), HotCategory.AUTOMOTIVE, 54_000_000L + offset * 50_000L, "" + (5400 + offset * 5) + "万",
                        3, "https://example.com/mock-3", null, null)
        );
    }
}
