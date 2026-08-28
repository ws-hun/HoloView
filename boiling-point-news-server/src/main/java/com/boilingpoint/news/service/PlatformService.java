package com.boilingpoint.news.service;

import com.boilingpoint.news.common.enums.HotSource;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.PlatformVO;

import java.util.List;

public interface PlatformService {

    List<PlatformVO> listEnabledPlatforms();

    List<HotItemVO> getPlatformHotItems(HotSource source, Integer limit);
}
