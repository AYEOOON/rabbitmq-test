package com.RabbitMQ.RabbitMQTest.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RegisterConsumer {

    @RabbitListener(queues = "register.queue")
    public void receive(String message) {
        String[] parts = message.split(":");
        String courseId = parts[0];
        String studentId = parts[1];

        System.out.println("수강신청 요청 수신: " + courseId + ", 학생: " + studentId);
        // 단순 출력
    }
}
