package com.hejiale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.context.UserContext;
import com.hejiale.common.exception.CreateLinkCodeException;
import com.hejiale.common.util.LinkUtils;
import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.po.Link;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.domain.vo.LinkCodeVO;
import com.hejiale.domain.vo.LogCountVO;
import com.hejiale.domain.vo.MonitorListDetialsVO;
import com.hejiale.domain.vo.MonitorListVO;
import com.hejiale.mapper.LinkMapper;
import com.hejiale.service.ILinkAccessLogService;
import com.hejiale.service.ILinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
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
        // 封装
        LinkCodeVO linkCodeVO = new LinkCodeVO();
        List<LinkCodeVO> linkCodeVOList = new ArrayList<>();
        linkCodeVO.setLinkTitle(createLinkDTO.getLinkTitle());
        linkCodeVO.setLinkCode(linkCode);
        linkCodeVOList.add(linkCodeVO);
        return linkCodeVOList;
    }

    @Override
    public List<MonitorListVO<MonitorListDetialsVO>> getLinkMonitorList() {
        // 获取当前用户id
        Long userId = UserContext.getUserId();
        // 查询Link表获取当前用户所有link集合
        List<Link> linkList = lambdaQuery()
                .eq(Link::getUserId, userId)
                .list();
        if (linkList.isEmpty()){
            return List.of();
        }

        // 获取linkId集合
        List<Long> linkIds = linkList.stream().map(Link::getId).toList();
        // 计算每条link的总访问次数
        List<LinkAccessLog> logList = new ArrayList<>();
        Map<String, LogCountVO> CountMap = new HashMap<>();
        List<LogCountVO> logCountList = linkAccessLogService.CountBatchLinkAccess(linkIds);
        if (logCountList.isEmpty()){
            log.info("该用户linkCode没有访问记录");
        }else {
            // 构建linkId与count的map映射关系
            CountMap = logCountList.stream().collect(Collectors.toMap(LogCountVO::getLinkId, log -> log));
            // 查询Link_access_log表获取访问记录数据集合
            logList = linkAccessLogService.getBatchLinkAccessLogList(linkIds);
        }

        // 封装
        // 封装MinitorListDetialsVO, 同时构建linkId与MinitorListDetialsVO集合的映射关系
        Map<String, List<MonitorListDetialsVO>> detialsListMap = logList.stream().map(log -> {
            MonitorListDetialsVO detailsVO = new MonitorListDetialsVO();
            BeanUtils.copyProperties(log, detailsVO);
            detailsVO.setClickTime(log.getCreateTime());
            return detailsVO;
        }).collect(Collectors.groupingBy(MonitorListDetialsVO::getLinkId));

        // 封装 MinitorListVOList
        Map<String, LogCountVO> finalCountMap = CountMap;
        List<MonitorListVO<MonitorListDetialsVO>> MonitorListVOList = linkList.stream().map(link -> {
            MonitorListVO<MonitorListDetialsVO> monitorListVO = new MonitorListVO<>();
            BeanUtils.copyProperties(link, monitorListVO);
            LogCountVO logCountVO = finalCountMap.get(link.getId().toString());
            monitorListVO.setClickCount(logCountVO != null ? logCountVO.getCount() : 0);
            monitorListVO.setClickRecords(detialsListMap.get(link.getId().toString()));
            return monitorListVO;
        }).collect(Collectors.toList());

        //返回
        return MonitorListVOList;
    }
}
