package com.RabbitMQ.RabbitMQTest.consumer;

import com.RabbitMQ.RabbitMQTest.dto.RegisterRequest;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Getter
@Service
public class RegisterConsumer{

    @RabbitListener(queues = "register.queue")
    public void consume(RegisterRequest request) {
        // 이 메서드 안이 Consumer 역할 (메시지를 소비 = 처리)
        System.out.println("receive: " + request.getCourseId() + "stu: " + request.getStudentId());
    }
}
