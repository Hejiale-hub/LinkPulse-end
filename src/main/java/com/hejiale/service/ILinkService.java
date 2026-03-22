package com.hejiale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.dto.MonitorPageDTO;
import com.hejiale.domain.dto.TitleDistributionDTO;
import com.hejiale.domain.po.Link;
import com.hejiale.domain.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * <p>
 * 短链接核心信息表 服务类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
public interface ILinkService extends IService<Link> {

    List<LinkCodeVO> createShortLink(CreateLinkDTO createLinkDTO);

    PageVO<MonitorListVO> getLinkMonitorListPage(MonitorPageDTO monitorPageDTO);

    PageVO<MonitorListDetialsVO> getLinkMonitorDetailRecords(MonitorPageDTO monitorPageDTO);

    List<TitleDistributionVO> getTitleDistribution(TitleDistributionDTO titleDistributionDTO);

    List<MonitorTrendVO> getMonitorTrend(MonitorPageDTO monitorPageDTO);

    String redirect(String linkCode, HttpServletRequest request, HttpServletResponse response);
}
