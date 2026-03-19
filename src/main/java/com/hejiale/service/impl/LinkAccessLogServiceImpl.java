package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.context.UserContext;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.domain.vo.LogCountVO;
import com.hejiale.mapper.LinkAccessLogMapper;
import com.hejiale.service.ILinkAccessLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<LogCountVO> CountBatchLinkAccess(List linkIds) {
        List<LogCountVO> logCountVOList = LogMapper.countBatchLinkAccess(linkIds);
        return logCountVOList;
    }
}
