package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.constants.UrlConstants;
import com.hejiale.common.context.UserContext;
import com.hejiale.common.domain.vo.CountLogVO;
import com.hejiale.common.exception.CreateLinkCodeException;
import com.hejiale.common.util.LinkUtils;
import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.dto.MonitorPageDTO;
import com.hejiale.domain.dto.TitleDistributionDTO;
import com.hejiale.domain.po.Link;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.domain.vo.*;
import com.hejiale.mapper.LinkAccessLogMapper;
import com.hejiale.mapper.LinkMapper;
import com.hejiale.service.ILinkAccessLogService;
import com.hejiale.service.ILinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * <p>
 * 短链接核心信息表 服务实现类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LinkServiceImpl extends ServiceImpl<LinkMapper, Link> implements ILinkService {
    private final ILinkAccessLogService linkAccessLogService;
    private final LinkAccessLogMapper linkAccessLogMapper;;

    @Transactional
    @Override
    public List<LinkCodeVO> createShortLink(CreateLinkDTO createLinkDTO) {
        // 属性拷贝
        Link link = new Link();
        link.setUserId(UserContext.getUserId());
        BeanUtils.copyProperties(createLinkDTO, link);
        // 保存linkCode对象到数据库，获取自增ID
        boolean result = save(link);
        if (!result) {
            throw new CreateLinkCodeException("创建链接码失败");
        }
        // 根据ID生成linkcode
        String linkCode = LinkUtils.encode(link.getId());
        // 更新短码回数据库
        link.setLinkCode(linkCode);
        updateById(link);

        // 添加链接url前缀
        StringBuilder codeUrl = new StringBuilder();
        codeUrl.append(UrlConstants.LINK_PREFIX);
        codeUrl.append(linkCode);

        // 封装
        LinkCodeVO linkCodeVO = new LinkCodeVO();
        List<LinkCodeVO> linkCodeVOList = new ArrayList<>();
        linkCodeVO.setLinkTitle(createLinkDTO.getLinkTitle());
        linkCodeVO.setLinkCode(codeUrl.toString());
        linkCodeVOList.add(linkCodeVO);
        return linkCodeVOList;
    }

    @Override
    public PageVO<MonitorListVO> getLinkMonitorListPage(MonitorPageDTO monitorPageDTO) {
        // 获取当前用户id
        Long userId = UserContext.getUserId();
        // 查询Link表获取当前用户link集合
        Page<Link> linkPage = lambdaQuery()
                .eq(Link::getUserId, userId)
                .like(monitorPageDTO.getLinkTitleKeyword() != null,
                        Link::getLinkTitle,
                        monitorPageDTO.getLinkTitleKeyword())
                .like(monitorPageDTO.getLinkCodeKeyword() != null,
                        Link::getLinkCode,
                        monitorPageDTO.getLinkCodeKeyword())
                .like(monitorPageDTO.getOriginalUrlKeyword() != null,
                        Link::getOriginalUrl,
                        monitorPageDTO.getOriginalUrlKeyword())
                .page(new Page<>(monitorPageDTO.getPageNo(), monitorPageDTO.getPageSize()));

        List<Link> linkList = linkPage.getRecords();
        if (linkList.isEmpty()){
            log.info("该用户没有创建过linkCode");
            PageVO<MonitorListVO> PageVO = new PageVO<>();
            PageVO.setTotal(0L);
            PageVO.setPageNo(monitorPageDTO.getPageNo());
            PageVO.setPageSize(monitorPageDTO.getPageSize());
            PageVO.setList(null);
            return PageVO;
        }

        // 查询Link_access_log表获取访问记录数据集合
        // 获取linkId集合
        List<Long> linkIds = linkList.stream().map(Link::getId).toList();
        List<CountLogVO> countLogVOList = linkAccessLogMapper.countLogByLink(linkIds);

        // 构建linkId与CountVO集合的映射关系
        Map<String, CountLogVO> countLogVOMap = countLogVOList.stream().collect(Collectors.toMap(CountLogVO::getLinkId, log -> log));

        // 封装 pageVO 和 MinitorListVO
        List<MonitorListVO> monitorListVOList = linkList.stream().map(link -> {
            MonitorListVO monitorListVO = new MonitorListVO();
            monitorListVO.setLinkId(link.getId());
            monitorListVO.setLinkCode(link.getLinkCode());
            monitorListVO.setLinkTitle(link.getLinkTitle());
            monitorListVO.setOriginalUrl(link.getOriginalUrl());

            // 处理没有访问记录的情况，避免空指针异常
            CountLogVO countLogVO = countLogVOMap.get(link.getId().toString());
            if (countLogVO != null) {
                monitorListVO.setClickCount(countLogVO.getClickCount());
                monitorListVO.setTopProvince(countLogVO.getTopProvince());
                monitorListVO.setLatestClickTime(countLogVO.getLatestClickTime());
            } else {
                // 没有访问记录时设置默认值
                monitorListVO.setClickCount(0L);
                monitorListVO.setTopProvince(null);
                monitorListVO.setLatestClickTime(null);
            }

            return monitorListVO;
        }).toList();

        PageVO<MonitorListVO> pageVO = new PageVO<>();
        pageVO.setTotal(linkPage.getTotal());
        pageVO.setPageNo(monitorPageDTO.getPageNo());
        pageVO.setPageSize(monitorPageDTO.getPageSize());
        pageVO.setList(monitorListVOList);

        // 返回
        return pageVO;
    }

    @Override
    public PageVO<MonitorListDetialsVO> getLinkMonitorDetailRecords(MonitorPageDTO monitorPageDTO) {
        // 获取linkId
        Link link = lambdaQuery()
                .eq(Link::getLinkCode, monitorPageDTO.getLinkCode())
                .one();
        Long linkId = link.getId();

        if (linkId == null) {
            log.error("未找到对应的linkId");
            throw new RuntimeException("linkCode异常错误");
        }

        // 获取link_access_log表数据
        LocalDate endDateExclusive = monitorPageDTO.getEndDate() == null ? null : monitorPageDTO.getEndDate().plusDays(1);
        Page<LinkAccessLog> logPage = linkAccessLogService.lambdaQuery()
                .eq(LinkAccessLog::getLinkId, linkId)
                .between(monitorPageDTO.getStartDate() != null && endDateExclusive != null,
                        LinkAccessLog::getCreateTime,
                        monitorPageDTO.getStartDate(),
                        endDateExclusive) // 处理日期范围，确保包含结束日期的整天
                .orderByDesc(LinkAccessLog::getCreateTime)
                .page(new Page<>(monitorPageDTO.getPageNo(), monitorPageDTO.getPageSize()));

        List<LinkAccessLog> logList = logPage.getRecords();

        // 封装 MonitorListDetialsVO
        List<MonitorListDetialsVO> DetialsVOList = logList.stream().map(log -> {
            MonitorListDetialsVO DetialsVO = new MonitorListDetialsVO();
            DetialsVO.setLinkId(log.getLinkId());
            DetialsVO.setIp(log.getIp());
            DetialsVO.setProvince(log.getProvince());
            DetialsVO.setCity(log.getCity());
            DetialsVO.setUa(log.getUa());
            DetialsVO.setOs(log.getOs());
            DetialsVO.setBrowser(log.getBrowser());
            DetialsVO.setClickTime(log.getCreateTime());
            return DetialsVO;
        }).toList();

        PageVO<MonitorListDetialsVO> pageVO = new PageVO<>();
        pageVO.setList(DetialsVOList);
        pageVO.setTotal(logPage.getTotal());
        pageVO.setPageNo(monitorPageDTO.getPageNo());
        pageVO.setPageSize(monitorPageDTO.getPageSize());

        // 返回
        return pageVO;
    }

    @Override
    public List<TitleDistributionVO> getTitleDistribution(TitleDistributionDTO titleDistributionDTO) {
        // 获取当前用户id
        Long userId = UserContext.getUserId();

        // 查询Link表获取当前用户link集合
        List<Link> linkList = lambdaQuery()
                .eq(Link::getUserId, userId)
                .like(titleDistributionDTO.getLinkTitleKeyword() != null,
                        Link::getLinkTitle,
                        titleDistributionDTO.getLinkTitleKeyword())
                .like(titleDistributionDTO.getLinkCodeKeyword() != null,
                        Link::getLinkCode,
                        titleDistributionDTO.getLinkCodeKeyword())
                .like(titleDistributionDTO.getOriginalUrlKeyword() != null,
                        Link::getOriginalUrl,
                        titleDistributionDTO.getOriginalUrlKeyword())
                .list();
        if (linkList.isEmpty()){
            log.info("该用户没有创建过linkCode");
            return Collections.emptyList();
        }
        List<Long> ids = linkList.stream().map(Link::getId).toList();
        // 构建id与linkTitle的映射关系
        Map<Long, String> linkIdTitleMap = linkList.stream().collect(Collectors.toMap(Link::getId, Link::getLinkTitle));

        List<LogCountVO> logCountList = linkAccessLogMapper.countLogForDistribution(ids, titleDistributionDTO);
        if (logCountList.isEmpty()) {
            log.info("没有访问记录");
            return Collections.emptyList();
        }

        // 封装
        return logCountList.stream().map(logCount -> {
            TitleDistributionVO titleDistributionVO = new TitleDistributionVO();
            titleDistributionVO.setLinkTitle(linkIdTitleMap.get(logCount.getLinkId()));
            titleDistributionVO.setClicks(logCount.getCount());
            return titleDistributionVO;
        }).toList();
    }

    @Override
    public List<MonitorTrendVO> getMonitorTrend(MonitorPageDTO monitorPageDTO) {
        // 获取当前用户id
        Long userId = UserContext.getUserId();

        // 查询Link表获取当前用户link集合
        List<Link> linkList = lambdaQuery()
                .eq(Link::getUserId, userId)
                .like(monitorPageDTO.getLinkTitleKeyword() != null,
                        Link::getLinkTitle,
                        monitorPageDTO.getLinkTitleKeyword())
                .like(monitorPageDTO.getLinkCodeKeyword() != null,
                        Link::getLinkCode,
                        monitorPageDTO.getLinkCodeKeyword())
                .like(monitorPageDTO.getOriginalUrlKeyword() != null,
                        Link::getOriginalUrl,
                        monitorPageDTO.getRegionKeyword())
                .list();
        if (linkList.isEmpty()){
            log.info("该用户没有创建过linkCode");
            return Collections.emptyList();
        }

        // 查询Link_access_log表获取访问记录数据集合
        // 获取linkId集合
        List<Long> linkIds = linkList.stream().map(Link::getId).toList();
        List<CountLogVO> countLogList = linkAccessLogMapper.countLogForTrend(linkIds, monitorPageDTO);

        // 封装返回
        return countLogList.stream().map(countLog -> {
            MonitorTrendVO monitorTrendVO = new MonitorTrendVO();
            monitorTrendVO.setTime(countLog.getTime());
            monitorTrendVO.setClicks(countLog.getClickCount());
            return monitorTrendVO;
        }).toList();
    }

    /**
     * 访问短链接，重定向到原始URL
     * @param linkCode 短链接code
     * @param request 请求对象
     * @param response 响应对象
     */
    @Override
    public String redirect(String linkCode, HttpServletRequest request, HttpServletResponse response) {
        // 根据shortCode查询Link表获取原始URL ,todo redis缓存查询
        Link link = lambdaQuery()
                .eq(Link::getLinkCode, linkCode)
                .select(Link::getOriginalUrl, Link::getId)
                .one();
        if (link == null) {
            throw new RuntimeException("链接不存在或已失效");
        }

        String url = link.getOriginalUrl();

        // 处理原始URL没有协议头的情况，默认添加http://
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        // 异步记录访问日志
        linkAccessLogService.asyncRecord(link.getId(), request);

        return url;
    }
}