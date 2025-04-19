package com.RabbitMQ.RabbitMQTest.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue registerQueue() {
        return new Queue("register.queue", false);
    }
}

