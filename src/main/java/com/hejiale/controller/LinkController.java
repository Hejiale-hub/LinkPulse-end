package com.hejiale.controller;


import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.dto.DeleteLinksDTO;
import com.hejiale.domain.dto.MonitorPageDTO;
import com.hejiale.domain.dto.TitleDistributionDTO;
import com.hejiale.domain.po.Link;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.domain.vo.*;
import com.hejiale.service.ILinkService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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
    @Tool(description = "创建短链接")
    @PostMapping("/createCode")
    public Result<List<LinkCodeVO>> createShortLink(@RequestBody @ToolParam(description = "创建短链接的对象参数") CreateLinkDTO createLinkDTO) {
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

    @Tool(description = "获取当前用户的所有短链接列表")
    @GetMapping("/getAllLinkList")
    public Result<List<Link>> getAllLinkList() {
        List<Link> linkList = shortLinkService.getAllLinkList();
        return Result.success(linkList);
    }

    @Tool(description = "查看当前用户的所有短链的访问记录")
    @GetMapping("/getAllLinkRecords")
    public Result<List<LinkAccessLog>> getAllLinkRecords() {
        List<LinkAccessLog> linkAccessLogList = shortLinkService.getAllLinkRecords();
        return Result.success(linkAccessLogList);
    }

    /**
     * 删除短链接
     */
    @Tool(description = "删除短链接")
    @DeleteMapping("/deleteByLinkIds")
    public Result<String> deleteLink(@RequestBody @ToolParam(description = "删除短链接接口的对象参数") DeleteLinksDTO deleteLinksDTO) {
        shortLinkService.deleteByLinkIds(deleteLinksDTO.getLinkIds());
        return Result.success();
    }
}
