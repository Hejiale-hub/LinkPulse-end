package com.hejiale.task;

import com.hejiale.common.Properties.VisitorTrackingProperties;
import com.hejiale.service.IVisitorStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 当天：按固定间隔把 Redis 快照 UPSERT 到库（不删 Redis）。<br>
 * 每日 0:05：归档「昨日」并删除对应 Redis Key。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorDayStatPersistScheduler {

    private static final ZoneId ZONE_SH = ZoneId.of("Asia/Shanghai");

    private final IVisitorStatService visitorStatService;
    private final VisitorTrackingProperties visitorTrackingProperties;

    @Scheduled(fixedRateString = "${link.visitor-tracking.sync-fixed-rate-ms:300000}")
    public void syncTodaySnapshotToDatabase() {
        if (!visitorTrackingProperties.isSyncEnabled()) {
            return;
        }
        LocalDate today = LocalDate.now(ZONE_SH);
        try {
            visitorStatService.upsertDayFromRedisKeepingRedis(today);
        } catch (Exception ex) {
            log.error("visitor snapshot sync failed date={}", today, ex);
        }
    }

    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Shanghai")
    public void persistYesterday() {
        LocalDate yesterday = LocalDate.now(ZONE_SH).minusDays(1);
        try {
            visitorStatService.persistDayFromRedisAndDelete(yesterday);
        } catch (Exception ex) {
            log.error("visitor persist failed date={}", yesterday, ex);
        }
    }
}
