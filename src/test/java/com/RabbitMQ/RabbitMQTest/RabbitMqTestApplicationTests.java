package com.RabbitMQ.RabbitMQTest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
class RabbitMqTestApplicationTests {
    @Test
    void contextLoads() {
        System.out.println("Hello, Spring");
    }
}
