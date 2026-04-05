package com.hejiale.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.Properties.UrlProperties;
import com.hejiale.common.constants.UrlConstants;
import com.hejiale.common.context.UserContext;
import com.hejiale.common.domain.po.RequestInfo;
import com.hejiale.common.domain.vo.CountLogVO;
import com.hejiale.common.exception.CreateLinkCodeException;
import com.hejiale.common.util.LinkUtils;
import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.dto.MonitorPageDTO;
import com.hejiale.domain.dto.TitleDistributionDTO;
import com.hejiale.domain.po.Link;
import com.hejiale.domain.po.LinkAccessLog;
import com.hejiale.domain.vo.*;
import com.hejiale.mapper.LinkAccessLogMapper;
import com.hejiale.mapper.LinkMapper;
import com.hejiale.service.ILinkAccessLogService;
import com.hejiale.service.ILinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hejiale.common.constants.MqConstants.*;
import static com.hejiale.common.constants.RedisConstants.*;


/**
 * <p>
 * 短链接核心信息表 服务实现类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LinkServiceImpl extends ServiceImpl<LinkMapper, Link> implements ILinkService {
    private final ILinkAccessLogService linkAccessLogService;
    private final LinkAccessLogMapper linkAccessLogMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RBloomFilter<String> bloomFilter;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redisson;
    private final RedissonClient redissonClient;
    private final UrlProperties urlProperties;

    @Transactional
    @Override
    public List<LinkCodeVO> createShortLink(CreateLinkDTO createLinkDTO) {
        // 属性拷贝
        Link link = new Link();
        link.setUserId(UserContext.getUserId());
        BeanUtils.copyProperties(createLinkDTO, link);
        // 保存linkCode对象到数据库，获取自增ID
        boolean result = save(link);
        if (!result) {
            throw new CreateLinkCodeException("创建链接码失败");
        }
        // 根据ID生成linkcode
        String linkCode = LinkUtils.encode(link.getId());
        // 更新短码回数据库
        link.setLinkCode(linkCode);
        updateById(link);
        // 将linkCode添加到布隆过滤器
        log.info("将linkCode添加到布隆过滤器，linkCode: {}", linkCode);
        bloomFilter.add(linkCode);

        // 添加链接url前缀
        StringBuilder codeUrl = new StringBuilder();
        codeUrl.append(urlProperties.getUrlPrefix());
        codeUrl.append(linkCode);

        // 封装
        LinkCodeVO linkCodeVO = new LinkCodeVO();
        List<LinkCodeVO> linkCodeVOList = new ArrayList<>();
        linkCodeVO.setLinkTitle(createLinkDTO.getLinkTitle());
        linkCodeVO.setLinkCode(codeUrl.toString());
        linkCodeVOList.add(linkCodeVO);
        return linkCodeVOList;
    }

    @Override
    public PageVO<MonitorListVO> getLinkMonitorListPage(MonitorPageDTO monitorPageDTO) {
        // 获取当前用户id
        Long userId = UserContext.getUserId();
        // 查询Link表获取当前用户link集合
        Page<Link> linkPage = lambdaQuery()
                .eq(Link::getUserId, userId)
                .like(monitorPageDTO.getLinkTitleKeyword() != null,
                        Link::getLinkTitle,
                        monitorPageDTO.getLinkTitleKeyword())
                .like(monitorPageDTO.getLinkCodeKeyword() != null,
                        Link::getLinkCode,
                        monitorPageDTO.getLinkCodeKeyword())
                .like(monitorPageDTO.getOriginalUrlKeyword() != null,
                        Link::getOriginalUrl,
                        monitorPageDTO.getOriginalUrlKeyword())
                .page(new Page<>(monitorPageDTO.getPageNo(), monitorPageDTO.getPageSize()));

        List<Link> linkList = linkPage.getRecords();
        if (linkList.isEmpty()){
            log.info("该用户没有创建过linkCode");
            PageVO<MonitorListVO> PageVO = new PageVO<>();
            PageVO.setTotal(0L);
            PageVO.setPageNo(monitorPageDTO.getPageNo());
            PageVO.setPageSize(monitorPageDTO.getPageSize());
            PageVO.setList(null);
            return PageVO;
        }

        // 查询Link_access_log表获取访问记录数据集合
        // 获取linkId集合
        List<Long> linkIds = linkList.stream().map(Link::getId).toList();
        List<CountLogVO> countLogVOList = linkAccessLogMapper.countLogByLink(linkIds);

        // 构建linkId与CountVO集合的映射关系
        Map<String, CountLogVO> countLogVOMap = countLogVOList.stream().collect(Collectors.toMap(CountLogVO::getLinkId, log -> log));

        // 封装 pageVO 和 MinitorListVO
        List<MonitorListVO> monitorListVOList = linkList.stream().map(link -> {
            MonitorListVO monitorListVO = new MonitorListVO();
            monitorListVO.setLinkId(link.getId());
            monitorListVO.setLinkCode(link.getLinkCode());
            monitorListVO.setLinkTitle(link.getLinkTitle());
            monitorListVO.setOriginalUrl(link.getOriginalUrl());

            // 处理没有访问记录的情况，避免空指针异常
            CountLogVO countLogVO = countLogVOMap.get(link.getId().toString());
            if (countLogVO != null) {
                monitorListVO.setClickCount(countLogVO.getClickCount());
                monitorListVO.setTopProvince(countLogVO.getTopProvince());
                monitorListVO.setLatestClickTime(countLogVO.getLatestClickTime());
            } else {
                // 没有访问记录时设置默认值
                monitorListVO.setClickCount(0L);
                monitorListVO.setTopProvince(null);
                monitorListVO.setLatestClickTime(null);
            }

            return monitorListVO;
        }).toList();

        PageVO<MonitorListVO> pageVO = new PageVO<>();
        pageVO.setTotal(linkPage.getTotal());
        pageVO.setPageNo(monitorPageDTO.getPageNo());
        pageVO.setPageSize(monitorPageDTO.getPageSize());
        pageVO.setList(monitorListVOList);

        // 返回
        return pageVO;
    }

    @Override
    public PageVO<MonitorListDetialsVO> getLinkMonitorDetailRecords(MonitorPageDTO monitorPageDTO) {
        // 获取linkId
        Link link = lambdaQuery()
                .eq(Link::getLinkCode, monitorPageDTO.getLinkCode())
                .one();
        Long linkId = link.getId();

        if (linkId == null) {
            log.error("未找到对应的linkId");
            throw new RuntimeException("linkCode异常错误");
        }

        // 获取link_access_log表数据
        LocalDate endDateExclusive = monitorPageDTO.getEndDate() == null ? null : monitorPageDTO.getEndDate().plusDays(1);
        Page<LinkAccessLog> logPage = linkAccessLogService.lambdaQuery()
                .eq(LinkAccessLog::getLinkId, linkId)
                .between(monitorPageDTO.getStartDate() != null && endDateExclusive != null,
                        LinkAccessLog::getCreateTime,
                        monitorPageDTO.getStartDate(),
                        endDateExclusive) // 处理日期范围，确保包含结束日期的整天
                .orderByDesc(LinkAccessLog::getCreateTime)
                .page(new Page<>(monitorPageDTO.getPageNo(), monitorPageDTO.getPageSize()));

        List<LinkAccessLog> logList = logPage.getRecords();

        // 封装 MonitorListDetialsVO
        List<MonitorListDetialsVO> DetialsVOList = logList.stream().map(log -> {
            MonitorListDetialsVO DetialsVO = new MonitorListDetialsVO();
            DetialsVO.setLinkId(log.getLinkId());
            DetialsVO.setIp(log.getIp());
            DetialsVO.setProvince(log.getProvince());
            DetialsVO.setCity(log.getCity());
            DetialsVO.setUa(log.getUa());
            DetialsVO.setOs(log.getOs());
            DetialsVO.setBrowser(log.getBrowser());
            DetialsVO.setClickTime(log.getCreateTime());
            return DetialsVO;
        }).toList();

        PageVO<MonitorListDetialsVO> pageVO = new PageVO<>();
        pageVO.setList(DetialsVOList);
        pageVO.setTotal(logPage.getTotal());
        pageVO.setPageNo(monitorPageDTO.getPageNo());
        pageVO.setPageSize(monitorPageDTO.getPageSize());

        // 返回
        return pageVO;
    }

    @Override
    public List<TitleDistributionVO> getTitleDistribution(TitleDistributionDTO titleDistributionDTO) {
        // 获取当前用户id
        Long userId = UserContext.getUserId();

        // 查询Link表获取当前用户link集合
        List<Link> linkList = lambdaQuery()
                .eq(Link::getUserId, userId)
                .like(titleDistributionDTO.getLinkTitleKeyword() != null,
                        Link::getLinkTitle,
                        titleDistributionDTO.getLinkTitleKeyword())
                .like(titleDistributionDTO.getLinkCodeKeyword() != null,
                        Link::getLinkCode,
                        titleDistributionDTO.getLinkCodeKeyword())
                .like(titleDistributionDTO.getOriginalUrlKeyword() != null,
                        Link::getOriginalUrl,
                        titleDistributionDTO.getOriginalUrlKeyword())
                .list();
        if (linkList.isEmpty()){
            log.info("该用户没有创建过linkCode");
            return Collections.emptyList();
        }
        List<Long> ids = linkList.stream().map(Link::getId).toList();
        // 构建id与linkTitle的映射关系
        Map<Long, String> linkIdTitleMap = linkList.stream().collect(Collectors.toMap(Link::getId, Link::getLinkTitle));

        List<LogCountVO> logCountList = linkAccessLogMapper.countLogForDistribution(ids, titleDistributionDTO);
        if (logCountList.isEmpty()) {
            log.info("没有访问记录");
            return Collections.emptyList();
        }

        // 封装
        return logCountList.stream().map(logCount -> {
            TitleDistributionVO titleDistributionVO = new TitleDistributionVO();
            titleDistributionVO.setLinkTitle(linkIdTitleMap.get(logCount.getLinkId()));
            titleDistributionVO.setClicks(logCount.getCount());
            return titleDistributionVO;
        }).toList();
    }

    @Override
    public List<MonitorTrendVO> getMonitorTrend(MonitorPageDTO monitorPageDTO) {
        // 获取当前用户id
        Long userId = UserContext.getUserId();

        // 查询Link表获取当前用户link集合
        List<Link> linkList = lambdaQuery()
                .eq(Link::getUserId, userId)
                .like(monitorPageDTO.getLinkTitleKeyword() != null,
                        Link::getLinkTitle,
                        monitorPageDTO.getLinkTitleKeyword())
                .like(monitorPageDTO.getLinkCodeKeyword() != null,
                        Link::getLinkCode,
                        monitorPageDTO.getLinkCodeKeyword())
                .like(monitorPageDTO.getOriginalUrlKeyword() != null,
                        Link::getOriginalUrl,
                        monitorPageDTO.getRegionKeyword())
                .list();
        if (linkList.isEmpty()){
            log.info("该用户没有创建过linkCode");
            return Collections.emptyList();
        }

        // 查询Link_access_log表获取访问记录数据集合
        // 获取linkId集合
        List<Long> linkIds = linkList.stream().map(Link::getId).toList();
        List<CountLogVO> countLogList = linkAccessLogMapper.countLogForTrend(linkIds, monitorPageDTO);

        // 封装返回
        return countLogList.stream().map(countLog -> {
            MonitorTrendVO monitorTrendVO = new MonitorTrendVO();
            monitorTrendVO.setTime(countLog.getTime());
            monitorTrendVO.setClicks(countLog.getClickCount());
            return monitorTrendVO;
        }).toList();
    }

    /**
     * 访问短链接，重定向到原始URL
     * @param linkCode 短链接code
     * @param request 请求对象
     * @param response 响应对象
     */
    @Override
    public String redirect(String linkCode, HttpServletRequest request, HttpServletResponse response) throws NotFoundException {
        // 根据shortCode查询Link表获取原始URL ,todo redis缓存查询
        // 1. 布隆过滤器拦截：如果判断不存在，则直接返回不存在
        if (!bloomFilter.contains(linkCode)) {
            log.info("布隆过滤器判断短链接不存在，linkCode: {}", linkCode);
            throw new NotFoundException("布隆过滤器判断短链接不存在");
        }
        log.info("布隆过滤器判断短链接可能存在，继续查询缓存和数据库，linkCode: {}", linkCode);
        String urlCacheKey = LINK_CODE_PREFIX_CACHE_KEY + linkCode;
        String idCacheKey = LINK_ID_PREFIX_CACHE_KEY + linkCode;
        // 2. 查询 Redis 缓存
        String originalUrl = null;
        log.info("查询 Redis 缓存，cacheKey: {}", urlCacheKey);
        try {
            originalUrl = redisTemplate.opsForValue().get(urlCacheKey);
        } catch (Exception e) {
            redisTemplate.delete(urlCacheKey);
        }
        if (StringUtils.isNotBlank(originalUrl)) {
            if (EMPTY_CACHE.equals(originalUrl)) {
                // 命中空值缓存（应对布隆过滤器以往的误判），直接返回
                throw new NotFoundException("短链接不存在");
            }
            log.info("命中 Redis 缓存，originalUrl: {}", originalUrl);
            // 发送MQ消息，异步记录访问日志到数据库
            log.info("发送MQ消息，异步记录访问日志到数据库，linkCode: {}", linkCode);
            Long linkId = getLinkIdFromCache(idCacheKey, linkCode);
            sendMqMessage(request, linkId);
            return originalUrl; // 命中正常缓存
        }
        // 3. 缓存未命中，查询数据库
        // 此处为了防止缓存击穿（并发查 DB），可以加上分布式锁，如 Redisson 的 getLock(cacheKey)
        String lockKey = "lock:" + urlCacheKey;
        RLock lock = redissonClient.getLock(lockKey);
        String url;
        Link link;
        try{
            // 加锁
            lock.lock();
            // 再次检查缓存，防止在获取锁的过程中其他线程已经将数据写入缓存（获取锁后，其他线程将会阻塞等待，从db查询到数据写入缓存后释放锁，其他线程会获取锁，为了防止重复查询数据库，所以需要在获取锁后再一次检查缓存是否有数据）
            originalUrl = redisTemplate.opsForValue().get(urlCacheKey);
            if (StringUtils.isNotBlank(originalUrl)) {
                if (EMPTY_CACHE.equals(originalUrl)) {
                    throw new NotFoundException("短链接不存在");
                }
                // 发送MQ消息，异步记录访问日志到数据库
                log.info("获取锁后再次检查，命中 Redis 缓存，originalUrl: {}", originalUrl);
                log.info("获取锁后再次检查，发送MQ消息，异步记录访问日志到数据库，linkCode: {}", linkCode);
                Long linkId = getLinkIdFromCache(idCacheKey, linkCode);
                sendMqMessage(request, linkId);
                return originalUrl;
            }
            // 查询数据库
            log.info("缓存未命中，查询数据库，linkCode: {}", linkCode);
            link = lambdaQuery()
                    .eq(Link::getLinkCode, linkCode)
                    .select(Link::getOriginalUrl, Link::getId)
                    .one();
            if (link == null) {
                // 数据库也不存在（布隆过滤器误判），写入空值，过期时间设短一点（如 5分钟）
                redisTemplate.opsForValue().set(urlCacheKey, EMPTY_CACHE, 5, TimeUnit.MINUTES);
                throw new RuntimeException("链接不存在或已失效");
            }
            url = link.getOriginalUrl();
            // 处理原始URL没有协议头的情况，默认添加http://
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            // 4. 数据库存在，同时写入 URL 缓存和 linkId 缓存
            // 设置过期时间：如 1 小时 + (0~10分钟随机数)，防止缓存雪崩
            long expireTime = 3600 + new Random().nextInt(600);
            redisTemplate.opsForValue().set(urlCacheKey, url, expireTime, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(idCacheKey, link.getId().toString(), expireTime, TimeUnit.SECONDS);
            log.info("数据库查询到数据，写入 Redis 缓存，cacheKey: {}, url: {}, expireTime: {}秒", urlCacheKey, url, expireTime);
            log.info("数据库查询到数据，写入 Redis 缓存，cacheKey: {}, linkId: {}, expireTime: {}秒", idCacheKey, link.getId(), expireTime);
        }finally {
            // 释放锁
            lock.unlock();
        }
        // 发送MQ消息，异步记录访问日志到数据库
        sendMqMessage(request, link.getId());
        return url;
    }

    /**
     * 构建异步消息对象，封装包含访问日志的必要信息，并发送MQ消息，异步保存日志到数据库
     * @param request 请求对象，包含访问日志的必要信息
     * @param linkId 访问的链接ID
     */
    private void sendMqMessage(HttpServletRequest request, Long linkId) {
        // 构建异步消息对象，封装包含访问日志的必要信息
        RequestInfo requestInfo = new RequestInfo();
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        requestInfo.setHeader(headers);
        requestInfo.setRemoteAddr(request.getRemoteAddr());
        requestInfo.setLinkId(linkId);
        // 发送MQ消息，异步保存日志到数据库
        rabbitTemplate.convertAndSend(MONITOR_EXCHANGE, LOGRECORD_ROUTING_KEY, requestInfo);
    }

    /**
     * 从缓存获取 linkId，如果缓存中没有，则从数据库查询（仅在缓存中没有 id 时触发，如旧缓存条目）
     */
    private Long getLinkIdFromCache(String idCacheKey, String linkCode) {
        String linkIdStr = redisTemplate.opsForValue().get(idCacheKey);
        if (linkIdStr != null) {
            return Long.parseLong(linkIdStr);
        }
        // 兜底：从数据库查询 linkId（仅在缓存中没有 id 时触发，如旧缓存条目）
        Link cacheLink = lambdaQuery()
                .eq(Link::getLinkCode, linkCode)
                .select(Link::getId)
                .one();
        return cacheLink != null ? cacheLink.getId() : null;
    }
}