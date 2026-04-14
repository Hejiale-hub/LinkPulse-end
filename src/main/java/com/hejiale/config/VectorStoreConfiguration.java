package com.hejiale.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore.MetadataField;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfiguration {

    @Bean
    public JedisPooled jedisPooled(RedisProperties redisProperties) {
        DefaultJedisClientConfig.Builder configBuilder = DefaultJedisClientConfig.builder()
                .database(redisProperties.getDatabase());

        if (redisProperties.getUsername() != null && !redisProperties.getUsername().isBlank()) {
            configBuilder.user(redisProperties.getUsername());
        }
        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isBlank()) {
            configBuilder.password(redisProperties.getPassword());
        }
        if (redisProperties.getTimeout() != null) {
            configBuilder.socketTimeoutMillis((int) redisProperties.getTimeout().toMillis());
            configBuilder.connectionTimeoutMillis((int) redisProperties.getTimeout().toMillis());
        }

        return new JedisPooled(
                new HostAndPort(redisProperties.getHost(), redisProperties.getPort()),
                configBuilder.build()
        );
    }

    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .initializeSchema(true)
                .indexName("spring-ai-index-v3")
                .prefix("doc:")
                .metadataFields(
                        MetadataField.tag("chatId")  // 增加chatId标签字段到向量数据库索引上，用于区分不同对话的pdf文档
                )
                .build();
    }
}
