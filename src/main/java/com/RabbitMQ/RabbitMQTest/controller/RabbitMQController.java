package com.RabbitMQ.RabbitMQTest.controller;

import com.RabbitMQ.RabbitMQTest.producer.MessageProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitMQController {
    private final MessageProducer producer;

    public RabbitMQController(MessageProducer producer) {
        this.producer = producer;
    }

    // 메시지를 보내기 위한 테스트 API
    @PostMapping("/send")
    public String send(@RequestParam String message) {
        producer.sendMessage(message);
        return "✅ 메시지 전송 완료: " + message;
    }
}
