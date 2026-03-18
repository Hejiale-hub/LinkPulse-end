package com.hejiale.mapper;

import com.hejiale.domain.po.LinkAccessLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hejiale.domain.vo.LogCountVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 短链接访问日志表，记录每次点击详情，用于统计分析 Mapper 接口
 * </p>
 *
 * @author hejiale
 * @since 2026-03-18
 */
public interface LinkAccessLogMapper extends BaseMapper<LinkAccessLog> {

    /**
     * 批量统计link访问量
     * @param linkIds
     */
    List<LogCountVO> CountBatchLinkAccess(@Param("linkIds") List linkIds);

}
