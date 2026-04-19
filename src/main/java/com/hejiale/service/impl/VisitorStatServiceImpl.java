package com.hejiale.service.impl;

import com.hejiale.domain.po.VisitorDayStat;
import com.hejiale.mapper.VisitorDayStatMapper;
import com.hejiale.service.IVisitorStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hejiale.common.constants.RedisConstants.VISITOR_DAY_KEY_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorStatServiceImpl implements IVisitorStatService {

    private static final ZoneId ZONE_SH = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DAY_KEY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int UPSERT_CHUNK = 500;

    private final StringRedisTemplate redisTemplate;
    private final VisitorDayStatMapper visitorDayStatMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void recordVisit(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return;
        }
        String ip = clientIp.trim();
        if (ip.length() > 45) {
            ip = ip.substring(0, 45);
        }
        LocalDate today = LocalDate.now(ZONE_SH);
        String key = VISITOR_DAY_KEY_PREFIX + today.format(DAY_KEY);
        redisTemplate.opsForHash().increment(key, ip, 1L);
        long ttlSeconds = secondsUntilEndOfDayPlusOneDay(ZONE_SH);
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    private static long secondsUntilEndOfDayPlusOneDay(ZoneId zone) {
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime endTomorrow = now.toLocalDate().plusDays(1).atStartOfDay(zone).plusDays(1);
        return Math.max(60L, Duration.between(now, endTomorrow).getSeconds());
    }

    @Override
    public void persistDayFromRedisAndDelete(LocalDate visitDate) {
        String key = VISITOR_DAY_KEY_PREFIX + visitDate.format(DAY_KEY);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            redisTemplate.delete(key);
            return;
        }
        List<VisitorDayStat> rows = buildRowsFromHashEntries(visitDate, key, entries);
        if (rows.isEmpty()) {
            redisTemplate.delete(key);
            return;
        }
        upsertChunks(rows);
        redisTemplate.delete(key);
        log.info("visitor day persisted date={} rows={} redisKeyRemoved=true", visitDate, rows.size());
    }

    @Override
    public void upsertDayFromRedisKeepingRedis(LocalDate visitDate) {
        String key = VISITOR_DAY_KEY_PREFIX + visitDate.format(DAY_KEY);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return;
        }
        List<VisitorDayStat> rows = buildRowsFromHashEntries(visitDate, key, entries);
        if (rows.isEmpty()) {
            return;
        }
        upsertChunks(rows);
        log.debug("visitor day snapshot upserted date={} rows={}", visitDate, rows.size());
    }

    private void upsertChunks(List<VisitorDayStat> rows) {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < rows.size(); i += UPSERT_CHUNK) {
                int end = Math.min(i + UPSERT_CHUNK, rows.size());
                visitorDayStatMapper.upsertBatch(rows.subList(i, end));
            }
        });
    }

    private static List<VisitorDayStat> buildRowsFromHashEntries(LocalDate visitDate, String key, Map<Object, Object> entries) {
        List<VisitorDayStat> rows = new ArrayList<>(entries.size());
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            String ip = e.getKey().toString().trim();
            if (!StringUtils.hasText(ip)) {
                continue;
            }
            long count;
            try {
                count = Long.parseLong(e.getValue().toString());
            } catch (NumberFormatException ex) {
                log.warn("visitor stat skip bad count key={} ip={} raw={}", key, ip, e.getValue());
                continue;
            }
            rows.add(new VisitorDayStat().setIp(ip).setVisitDate(visitDate).setVisitCount(count));
        }
        return rows;
    }
}
