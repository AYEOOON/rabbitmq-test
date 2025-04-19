package com.RabbitMQ.RabbitMQTest.controller;

import com.RabbitMQ.RabbitMQTest.dto.RegisterRequest;
import com.RabbitMQ.RabbitMQTest.producer.RegisterProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterProducer producer;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        producer.sendRegisterRequest(request);
        return ResponseEntity.ok("메시지 전송 완료");
    }
}
