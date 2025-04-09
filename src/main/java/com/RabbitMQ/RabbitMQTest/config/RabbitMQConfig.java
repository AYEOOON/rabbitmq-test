package com.RabbitMQ.RabbitMQTest.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // application.yml에서 값 주입
    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // Queue Bean 생성
    @Bean
    public Queue queue() {
        return new Queue(queueName);
    }

    // Exchange Bean 생성 (직접 연결되는 DirectExchange 사용)
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    // Queue와 Exchange를 라우팅 키로 바인딩
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
