package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.domain.po.RequestInfo;
import com.hejiale.common.util.IpUtils;
import com.hejiale.common.util.UaUtils;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.domain.vo.LogCountVO;
import com.hejiale.mapper.LinkAccessLogMapper;
import com.hejiale.service.ILinkAccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

import static com.hejiale.common.constants.MqConstants.*;

/**
 * <p>
 * 短链接访问日志表，记录每次点击详情，用于统计分析 服务实现类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@RequiredArgsConstructor
@Service
public class LinkAccessLogServiceImpl extends ServiceImpl<LinkAccessLogMapper, LinkAccessLog> implements ILinkAccessLogService {
    private final LinkAccessLogMapper LogMapper;

    /**
     * 根据linkId获取访问日志
     */
    public List<LinkAccessLog> getBatchLinkAccessLogList(List linkIds) {
        List<LinkAccessLog> list = lambdaQuery()
                .in(LinkAccessLog::getLinkId, linkIds)
                .list();
        return list;
    }

    /**
     * 根据linkId集合，批量获取link的访问次数
     */
    @Override
    public List<LogCountVO> CountBatchLinkAccess(List linkIds) {
        List<LogCountVO> logCountVOList = LogMapper.countBatchLinkAccess(linkIds);
        return logCountVOList;
    }

    /**
     * 异步记录访问日志
     */
    @RabbitListener(queues = LOGRECORD_QUEUE)
    @Override
    public void asyncRecord(RequestInfo requestInfo) {
        // 构建访问日志对象
        LinkAccessLog log = buildLog(requestInfo);
        // 直接保存访问日志到数据库
        boolean save = save(log);
        if (!save) {
            System.err.println("保存访问日志失败: " + log);
        }
    }

    /**
     * 构建访问日志对象，包含IP解析和UA解析
     * @return 构建好的访问日志对象
     */
    private LinkAccessLog buildLog(RequestInfo requestInfo) {
        LinkAccessLog log = new LinkAccessLog();

        // 1. 获取基础信息
        String ip = IpUtils.getIpAddress(requestInfo);
        String ua = requestInfo.getHeader().get("user-agent");


        // ===== IP 解析（省市） =====
        try {
            // 2. 解析 IP 属地 (ip2region 返回格式例如：中国|0|广东省|深圳市|电信)
            String regionStr = IpUtils.getRegion(ip);
            String province = "未知";
            String city = "未知";
            if (regionStr != null && regionStr.contains("|")) {
                String[] regions = regionStr.split("\\|");
                if (regions.length >= 4) {
                    province = regions[2]; // 省份
                    city = regions[3];     // 城市
                }
            }
            log.setProvince(province);
            log.setCity(city);
        } catch (Exception e) {
            log.setProvince("未知");
            log.setCity("未知");
        }

        // 3. 解析 User-Agent
        UserAgent uaInfo = UaUtils.parse(ua);
        // 获取操作系统和浏览器名称
        String os = uaInfo.getValue(UserAgent.OPERATING_SYSTEM_NAME_VERSION_MAJOR);
        String browser = uaInfo.getValue(UserAgent.AGENT_NAME_VERSION_MAJOR);

        // 封装其他字段
        log.setLinkId(requestInfo.getLinkId());
        log.setIp(ip);
        log.setUa(String.valueOf(ua));
        log.setCreateTime(LocalDateTime.now());
        log.setBrowser(browser);
        log.setOs(os);

        return log;
    }
}
