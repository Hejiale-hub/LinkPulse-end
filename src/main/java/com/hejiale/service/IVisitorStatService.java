package com.hejiale.service;

import java.time.LocalDate;

/**
 * 访客按日访问次数：Redis 计数与落库
 */
public interface IVisitorStatService {

    void recordVisit(String clientIp);

    /**
     * 将指定自然日的 Redis 汇总写入数据库，成功后删除该日 Redis Key（仅用于已结束的日期，例如昨日）。
     */
    void persistDayFromRedisAndDelete(LocalDate visitDate);

    /**
     * 将指定自然日 Redis 中的计数全量 UPSERT 到数据库，不删除 Redis（用于当天增量刷新库里的展示数据）。
     */
    void upsertDayFromRedisKeepingRedis(LocalDate visitDate);
}
