package com.hejiale.service.impl;

import com.hejiale.domain.po.VisitorDayStat;
import com.hejiale.mapper.VisitorDayStatMapper;
import com.hejiale.service.IVisitorStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    /**
     * HINCRBY + EXPIRE 合并为一次 EVAL，避免两次往返；
     * 同时绕开 Redisson + spring-data-redis 的 pExpire 栈溢出兼容性问题。
     */
    private static final RedisScript<Long> RECORD_VISIT_SCRIPT = new DefaultRedisScript<>(
            // call表示执行Redis原生命令，HINCRBY表示对哈希表中的字段值进行递增操作，
            // KEYS[1]表示第一个键（即visitor:day:{yyyyMMdd}，如果传入了多个键，那么就需要写循环便利的lua语法），
            // ARGV[1]表示第一个参数（即IP地址），1表示递增的值
            "local v = redis.call('HINCRBY', KEYS[1], ARGV[1], 1) " +
                    "redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
                    "return v",
            Long.class);

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
        long ttlSeconds = secondsUntilEndOfDayPlusOneDay(ZONE_SH);
        redisTemplate.execute(
                RECORD_VISIT_SCRIPT, // 要执行的 Lua 脚本
                Collections.singletonList(key), // 脚本中使用的 Redis 键列表，这里只有一个键，即 visitor:day:{yyyyMMdd}
                ip, // ARGV[1]，即要递增的字段（IP地址）
                Long.toString(ttlSeconds)); // ARGV[2]，即过期时间（秒）
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
