package com.RabbitMQ.RabbitMQTest.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {
    // 큐에서 메시지가 수신되면 자동으로 호출됨
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveMessage(String message) {
        System.out.println("📩 [Consumer] Received message = " + message);
    }
}
