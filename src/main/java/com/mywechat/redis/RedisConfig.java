package com.mywechat.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import static okhttp3.internal.Internal.logger;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-23 21:49
 */
@Configuration
public class RedisConfig<V> {//redis配置

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.host:}") // 提供了默认值 localhost
    private String redisHost;

    @Value("${spring.redis.port:}") // 提供了默认值 6379
    private Integer redisPort;

    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
            RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;
        } catch (Exception e) {
            logger.error("Redis配置错误, 请检查Redis配置", e);
            return null;
        }
    }
    @Bean("redisTemplate")
    public RedisTemplate<String,V> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,V> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());//设置key序列化方式
        template.setValueSerializer(RedisSerializer.json());//设置value序列化方式
        template.setHashKeySerializer(RedisSerializer.string());//设置hashkey序列化方式
        template.setHashValueSerializer(RedisSerializer.json());//设置hashvalue序列化方式
        template.afterPropertiesSet();
        return template;
    }
}
