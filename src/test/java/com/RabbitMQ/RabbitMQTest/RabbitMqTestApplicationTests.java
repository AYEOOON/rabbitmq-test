package com.RabbitMQ.RabbitMQTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.RabbitMQ.RabbitMQTest.config.RabbitConfig;
import com.RabbitMQ.RabbitMQTest.consumer.RegisterConsumer;
import com.RabbitMQ.RabbitMQTest.dto.RegisterRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
public class RabbitMqTestApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RegisterConsumer consumer;

    @Test
    void sendMessageAndAwaitConsume() throws InterruptedException {
        RegisterRequest request = new RegisterRequest("CS101", "U123");

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                request
        );
        System.out.println("✅ 메시지 전송 완료");
        Thread.sleep(5000);
    }

    @Test
    void sendBulkMessagesToQueue() throws InterruptedException {
        for (int i = 0; i < 5000; i++) {
            String studentId = "U" + String.format("%03d", i);
            RegisterRequest request = new RegisterRequest("CS101", studentId);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY,
                    request
            );

            System.out.println("✅ 메시지 전송: " + request.getStudentId() + " -> " + request.getCourseId());
        }

        System.out.println("✅ 1000건 메시지 전송 완료");
        Thread.sleep(5000);
    }
}
