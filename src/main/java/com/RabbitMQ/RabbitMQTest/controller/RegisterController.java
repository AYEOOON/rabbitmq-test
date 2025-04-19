package com.RabbitMQ.RabbitMQTest.controller;

import com.RabbitMQ.RabbitMQTest.config.RabbitConfig;
import com.RabbitMQ.RabbitMQTest.dto.RegisterRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final RabbitTemplate rabbitTemplate;

    public RegisterController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/register")
    public String registerCourse(@RequestBody RegisterRequest request) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                request
        );
        return "✅ 메시지 전송 완료: "
                + request.getStudentId() + " -> " + request.getCourseId();
    }
}