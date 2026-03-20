package com.hejiale.controller;


import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.dto.MonitorPageDTO;
import com.hejiale.domain.dto.TitleDistributionDTO;
import com.hejiale.domain.vo.*;
import com.hejiale.service.ILinkService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 短链接核心信息表 前端控制器
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@Api(tags = "link相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/link")
public class LinkController {
    private final ILinkService shortLinkService;
    /**
     * 创建短链接
     * @param createLinkDTO 创建短链接参数
     * @return 短链接列表
     */
    @PostMapping("/createCode")
    public Result<List<LinkCodeVO>> createShortLink(@RequestBody CreateLinkDTO createLinkDTO) {
        List<LinkCodeVO> linkCodeVOList = shortLinkService.createShortLink(createLinkDTO);
        return Result.success(linkCodeVOList);
    }
    /**
     * 获取link监控列表
     */
    @GetMapping("/monitorList")
    public Result<PageVO<MonitorListVO>> getLinkMonitorListPage(MonitorPageDTO monitorPageDTO) {
        PageVO<MonitorListVO> monitorListPageVO = shortLinkService.getLinkMonitorListPage(monitorPageDTO);
        return Result.success(monitorListPageVO);
    }

    /**
     * 获取link监控详情列表
     */
    @GetMapping("/monitorDetailRecords")
    public Result<PageVO<MonitorListDetialsVO>> getLinkMonitorDetailRecords(MonitorPageDTO monitorPageDTO) {
        PageVO<MonitorListDetialsVO> MonitorDetailPageVO = shortLinkService.getLinkMonitorDetailRecords(monitorPageDTO);
        return Result.success(MonitorDetailPageVO);
    }
    /**
     * 链接访问分布情况（饼图）
     */
    @GetMapping("/monitorLinkTitleDistribution")
    public Result<List<TitleDistributionVO>> getTitleDistribution(TitleDistributionDTO titleDistributionDTO) {
        List<TitleDistributionVO> titleDistributionVOList = shortLinkService.getTitleDistribution(titleDistributionDTO);
        return Result.success(titleDistributionVOList);
    }

    @GetMapping("/monitorTrend")
    public Result<List<MonitorTrendVO>> getMonitorTrend(MonitorPageDTO monitorPageDTO) {
        List<MonitorTrendVO> monitorTrendVOList = shortLinkService.getMonitorTrend(monitorPageDTO);
        return Result.success(monitorTrendVOList);
    }
}
