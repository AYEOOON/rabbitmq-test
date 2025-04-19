package com.RabbitMQ.RabbitMQTest.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // Redis가 로컬 기본 포트라면
        config.useSingleServer().setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }
}