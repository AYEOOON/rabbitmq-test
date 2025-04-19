package com.RabbitMQ.RabbitMQTest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.RabbitMQ.RabbitMQTest.config.RabbitConfig;
import com.RabbitMQ.RabbitMQTest.consumer.RegisterConsumer;
import com.RabbitMQ.RabbitMQTest.dto.RegisterRequest;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
public class RabbitMqTestApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RegisterConsumer consumer;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitAdmin rabbitAdmin;


    private static final String COURSE_ID = "CS101";
    private static final int MAX_CAPACITY = 30;

    @BeforeEach
    void purgeQueue() {
        // 큐 비우기
        rabbitAdmin.purgeQueue(RabbitConfig.QUEUE_NAME, true);
    }
    @BeforeEach
    void resetRedisState() {
        // Redis 초기화
        String countKey = "course:" + COURSE_ID + ":count";
        redisTemplate.delete(countKey);
    }

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

    @Test
    void sendMessageAndAwaitConsumeWithRedis() throws InterruptedException {
        RegisterRequest request = new RegisterRequest(COURSE_ID, "U123");

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                request
        );

        System.out.println("✅ 메시지 전송 완료");

        // Consumer에서 latch.countDown()으로 신호가 오길 기다림
        boolean completed = consumer.getLatch().await(5, TimeUnit.SECONDS);
        assertTrue(completed, "⏰ Consumer 응답 대기시간 초과");

        // Redis에서 증가 확인
        String countKey = "course:" + COURSE_ID + ":count";
        int count = Optional.of(redisTemplate.opsForValue().get(countKey))
                .map(Integer::parseInt)
                .orElse(0);

        assertEquals(1, count, "🎯 수강신청 인원이 1명이어야 합니다.");
    }

    @Test
    void sendBulkMessagesAndVerifyLimit() throws InterruptedException {
        // 500명 신청 시도 → 정원은 30명
        for (int i = 0; i < 500; i++) {
            String studentId = "U" + String.format("%03d", i);
            RegisterRequest request = new RegisterRequest(COURSE_ID, studentId);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY,
                    request
            );
        }

        // 모든 메시지가 처리될 시간을 기다림 (메시지 큐 기반이라 약간 여유)
        Thread.sleep(5000);

        String countKey = "course:" + COURSE_ID + ":count";
        int count = Optional.ofNullable(redisTemplate.opsForValue().get(countKey))
                .map(Integer::parseInt)
                .orElse(0);

        System.out.println("📊 최종 수강신청 인원: " + count);
        assertEquals(MAX_CAPACITY, count, "🎯 최대 수강 인원은 " + MAX_CAPACITY + "명이어야 합니다.");
    }

    @Test
    void sendBulkMessagesNoLockAndObserveOverflow() throws InterruptedException {
        // 300명 동시 신청
        for (int i = 0; i < 300; i++) {
            String studentId = "U" + String.format("%03d", i);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY,
                    new RegisterRequest(COURSE_ID, studentId)
            );
        }
        System.out.println("✅ 100건 메시지 전송 완료 (락 없음)");

        // 충분히 소비될 시간 대기
        Thread.sleep(5000);

        String countKey = "course:" + COURSE_ID + ":count";
        // 최종 count 읽기
        int finalCount = Optional.ofNullable(redisTemplate.opsForValue().get(countKey))
                .map(Integer::parseInt)
                .orElse(0);
        System.out.println("📊 (NoLock) 최종 수강신청 인원: " + finalCount);
        // 정원 30을 훌쩍 넘어서는지 확인
    }
}
