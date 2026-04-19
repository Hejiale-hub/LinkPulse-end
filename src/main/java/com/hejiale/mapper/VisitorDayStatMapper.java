package com.hejiale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hejiale.domain.po.VisitorDayStat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VisitorDayStatMapper extends BaseMapper<VisitorDayStat> {

    void upsertBatch(@Param("list") List<VisitorDayStat> list);
}
