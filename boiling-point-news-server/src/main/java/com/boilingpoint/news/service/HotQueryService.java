package com.boilingpoint.news.service;

import com.boilingpoint.news.dto.HotItemQueryDTO;
import com.boilingpoint.news.dto.HotSearchQueryDTO;
import com.boilingpoint.news.dto.HotTrendQueryDTO;
import com.boilingpoint.news.vo.HotDetailVO;
import com.boilingpoint.news.vo.HotItemVO;
import com.boilingpoint.news.vo.HotTrendPointVO;

import java.util.List;

public interface HotQueryService {

    List<HotItemVO> list(HotItemQueryDTO query);

    HotDetailVO getDetail(Long id);

    List<HotItemVO> ranking(Integer limit);

    List<HotItemVO> trending(Integer limit);

    List<HotItemVO> latest(Integer limit);

    List<HotItemVO> search(HotSearchQueryDTO query);

    List<HotTrendPointVO> getTrendPoints(Long hotId, HotTrendQueryDTO query);
}
