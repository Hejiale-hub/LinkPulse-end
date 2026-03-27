package com.hejiale.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Bean
    public RBloomFilter<String> linkCodeBloomFilter() {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("link_code_bloom_filter");
        // 初始化布隆过滤器，预计元素数量 10万，误判率 0.01
        // 注意：生产环境中通常只在第一次初始化，如果已存在则无需 tryInit
        bloomFilter.tryInit(100000L, 0.01);
        return bloomFilter;
    }
}

