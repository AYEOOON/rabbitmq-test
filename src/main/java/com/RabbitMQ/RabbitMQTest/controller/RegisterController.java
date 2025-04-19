package com.RabbitMQ.RabbitMQTest.controller;

import com.RabbitMQ.RabbitMQTest.producer.RegisterProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterProducer producer;

    @PostMapping("/register")
    public String register(@RequestParam String courseId, @RequestParam String studentId) {
        producer.send(courseId, studentId);
        return "수강신청 요청 전송 완료!";
    }
}
