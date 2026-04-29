package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.domain.po.RequestInfo;
import com.hejiale.common.util.IpUtils;
import com.hejiale.common.util.UaUtils;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.domain.vo.LogCountVO;
import com.hejiale.mapper.LinkAccessLogMapper;
import com.hejiale.service.ILinkAccessLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
@Slf4j
@Service
public class LinkAccessLogServiceImpl extends ServiceImpl<LinkAccessLogMapper, LinkAccessLog> implements ILinkAccessLogService {
    private final LinkAccessLogMapper LogMapper;
    // private final RabbitTemplate rabbitTemplate;

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
    @Override
    public void asyncRecord(RequestInfo requestInfo) {
        LinkAccessLog logEntity = buildLog(requestInfo);
        boolean saved = save(logEntity);
        if (!saved) {
            throw new RuntimeException("保存访问日志失败");
        }
    }

    // RabbitMQ todo 消息监听消费者，由于服务器资源有限，暂时不使用消息队列，以下代码已注释掉，后续有条件时取消注释即可使用mq记录访问日志 ！！！！
//    @RabbitListener(queues = LOGRECORD_QUEUE, containerFactory = "rabbitListenerContainerFactory")
//    public void asyncRecordWithAck(RequestInfo requestInfo, Message message, Channel channel) throws IOException {
//        long deliveryTag = message.getMessageProperties().getDeliveryTag();
//        try {
//            asyncRecord(requestInfo);
//            channel.basicAck(deliveryTag, false);
//        } catch (Exception e) {
//            int currentRetryCount = getRetryCount(message);
//            if (currentRetryCount < MAX_RETRY_COUNT) {
//                int nextRetryCount = currentRetryCount + 1;
//                String messageId = resolveMessageId(message);
//                rabbitTemplate.convertAndSend(RETRY_EXCHANGE, LOGRECORD_RETRY_ROUTING_KEY, requestInfo, msg -> {
//                    msg.getMessageProperties().setMessageId(messageId);
//                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//                    msg.getMessageProperties().setHeader(RETRY_COUNT_HEADER, nextRetryCount);
//                    return msg;
//                }, new CorrelationData(messageId));
//                channel.basicAck(deliveryTag, false);
//                log.warn("访问日志消费失败，发送到重试队列, retryCount={}, messageId={}, error={}",
//                        nextRetryCount, messageId, e.getMessage());
//                return;
//            }
//            String messageId = resolveMessageId(message);
//            rabbitTemplate.convertAndSend(DLX_EXCHANGE, LOGRECORD_DLQ_ROUTING_KEY, requestInfo, msg -> {
//                msg.getMessageProperties().setMessageId(messageId);
//                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//                msg.getMessageProperties().setHeader(RETRY_COUNT_HEADER, currentRetryCount);
//                return msg;
//            }, new CorrelationData(messageId));
//            channel.basicAck(deliveryTag, false);
//            log.error("访问日志消费失败并进入DLQ, retryCount={}, messageId={}, error={}",
//                    currentRetryCount, messageId, e.getMessage(), e);
//        }
//    }
//
//    private int getRetryCount(Message message) {
//        Map<String, Object> headers = message.getMessageProperties().getHeaders();
//        Object retryHeader = headers.get(RETRY_COUNT_HEADER);
//        if (retryHeader instanceof Number number) {
//            return number.intValue();
//        }
//        if (retryHeader instanceof String retryStr) {
//            try {
//                return Integer.parseInt(retryStr);
//            } catch (NumberFormatException ignore) {
//                return 0;
//            }
//        }
//        return 0;
//    }
//
//    private String resolveMessageId(Message message) {
//        String messageId = message.getMessageProperties().getMessageId();
//        return messageId == null || messageId.isBlank() ? UUID.randomUUID().toString() : messageId;
//    }

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
            // 2. 解析 IP 属地 (ip2region 返回格式例如：中国|区域|广东省|深圳市|电信)
            String regionStr = IpUtils.getRegion(ip);
            String province = "未知";
            String city = "未知";
            if (regionStr != null && regionStr.contains("|")) {
                String[] regions = regionStr.split("\\|");
                if (regions.length >= 4) {
                    province = regions[1]; // 省份
                    city = regions[2];     // 城市
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
