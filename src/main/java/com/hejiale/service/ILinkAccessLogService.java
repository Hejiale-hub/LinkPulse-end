package com.hejiale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hejiale.domain.po.LinkAccessLog;

/**
 * <p>
 * 短链接访问日志表，记录每次点击详情，用于统计分析 服务类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
public interface ILinkAccessLogService extends IService<LinkAccessLog> {

}
