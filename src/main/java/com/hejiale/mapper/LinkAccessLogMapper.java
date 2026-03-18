package com.hejiale.mapper;

import com.hejiale.domain.po.LinkAccessLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 短链接访问日志表，记录每次点击详情，用于统计分析 Mapper 接口
 * </p>
 *
 * @author hejiale
 * @since 2026-03-18
 */
public interface LinkAccessLogMapper extends BaseMapper<LinkAccessLog> {

}
