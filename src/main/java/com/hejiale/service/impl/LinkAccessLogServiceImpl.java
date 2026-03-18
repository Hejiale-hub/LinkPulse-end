package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.mapper.LinkAccessLogMapper;
import com.hejiale.service.ILinkAccessLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 短链接访问日志表，记录每次点击详情，用于统计分析 服务实现类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@Service
public class LinkAccessLogServiceImpl extends ServiceImpl<LinkAccessLogMapper, LinkAccessLog> implements ILinkAccessLogService {

}
