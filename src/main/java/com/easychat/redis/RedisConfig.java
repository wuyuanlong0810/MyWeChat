package com.easychat.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-23 21:49
 */
@Configuration
public class RedisConfig<V> {
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
