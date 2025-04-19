package com.RabbitMQ.RabbitMQTest.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterProducer {
    private final RabbitTemplate rabbitTemplate;

    public void send(String courseId, String studentId) {
        String message = courseId + ":" + studentId;
        rabbitTemplate.convertAndSend("register.queue", message);
    }
}