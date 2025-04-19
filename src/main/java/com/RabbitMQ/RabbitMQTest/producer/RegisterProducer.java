package com.RabbitMQ.RabbitMQTest.producer;

import com.RabbitMQ.RabbitMQTest.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendRegisterRequest(RegisterRequest request) {
        rabbitTemplate.convertAndSend("register.exchange", "register.routing.key", request);
    }
}