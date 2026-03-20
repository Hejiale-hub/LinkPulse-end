package com.hejiale.mapper;

import com.hejiale.common.domain.vo.CountLogVO;
import com.hejiale.domain.dto.MonitorPageDTO;
import com.hejiale.domain.dto.TitleDistributionDTO;
import com.hejiale.domain.po.LinkAccessLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hejiale.domain.vo.LogCountVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 短链接访问日志表，记录每次点击详情，用于统计分析 Mapper 接口
 * </p>
 *
 * @author hejiale
 * @since 2026-03-18
 */
public interface LinkAccessLogMapper extends BaseMapper<LinkAccessLog> {

    /**
     * 批量统计link访问量
     * @param linkIds
     */
    List<LogCountVO> countBatchLinkAccess(@Param("linkIds") List linkIds);

    /**
     * 聚合统计link访问量
     * @param linkIds
     */
    List<CountLogVO> countLogByLink(@Param("linkIds") List<Long> linkIds);


    List<LogCountVO> countLogForDistribution(@Param("ids") List<Long> ids, @Param("dto") TitleDistributionDTO titleDistributionDTO);

    List<CountLogVO> countLogForTrend(@Param("linkIds") List<Long> linkIds, @Param("dto") MonitorPageDTO monitorPageDTO);
}
