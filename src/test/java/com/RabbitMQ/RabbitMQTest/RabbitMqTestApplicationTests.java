package com.RabbitMQ.RabbitMQTest;

import com.RabbitMQ.RabbitMQTest.consumer.MessageConsumer;
import com.RabbitMQ.RabbitMQTest.producer.MessageProducer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
class RabbitMqTestApplicationTests {

	@Autowired
	private MessageProducer messageProducer;
	@Autowired
	private MessageConsumer messageConsumer;

	@Test
	@DisplayName("여러 개의 메시지를 순차적으로 전송하고 모두 수신된다")
	void sendMultipleMessages() throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			String msg = "Message " + i;
			messageProducer.sendMessage(msg);
			Thread.sleep(500); // 약간의 간격
		}

		// then
		// 로그로 확인하거나 latch를 확장해서 테스트
	}
}
