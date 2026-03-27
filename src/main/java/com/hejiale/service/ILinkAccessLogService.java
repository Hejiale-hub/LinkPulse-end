package com.hejiale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.domain.vo.LogCountVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 * 短链接访问日志表，记录每次点击详情，用于统计分析 服务类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
public interface ILinkAccessLogService extends IService<LinkAccessLog> {
    /**
     * 批量获取短链接访问日志
     * @param linkIds 短链接id集合
     * @return 短链接访问日志集合
     */
    List<LinkAccessLog> getBatchLinkAccessLogList(List linkIds);

    /**
     * 批量获取link的访问次数
     */
    List<LogCountVO> CountBatchLinkAccess(List linkIds);

    /**
     * 异步记录访问日志
     */
    void asyncRecord(Long linkId, HttpServletRequest request);
}
