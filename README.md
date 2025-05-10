# RabbitMQ와 Redis로 수강 신청 시스템의 동시성 제어

다수의 사용자가 동시에 수강신청을 시도하는 상황을 시뮬레이션하고, 그로 인한 문제를 해결하는 방법을 단계적으로 적용

## 1. 기본: 수강신청 시스템 구축

- 기본적인 수강신청 시스템을 간단하게 구현하여, 사용자가 수강신청을 요청하고 이를 처리할 수 있는 로직 구현
- 기본적인 수신과 처리 로직만 포함하고, 큐나 락을 사용X
- 동시성 제어 없음 → 중복 신청, 정원 초과 가능성 존재

```
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    // 수강신청 요청을 받아 단순히 응답 반환
    public String registerCourse(RegistrationRequest request) {
        return "수강신청 성공! " + request.getCourseName();
    }
}
```
-> 수강신청 처리 로직, 단순히 신청이 되었다는 메시지 반환


## 2.많아진 수강신청의 트래픽 처리 (MQ를 통한 비동기 처리)
- 많아진 트래픽을 처리하기 위해, 수강신청 요청을 **RabbitMQ**에 넣어 비동기적으로 처리
- RabbitMQ를 사용하여 요청을 큐에 넣고, 이를 소비자가 비동기적으로 처리

**RabbitMQ 설정**
```
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // 'course-registration-queue'라는 이름의 큐 생성
    @Bean
    public Queue registrationQueue() {
        return new Queue("course-registration-queue", false);
    }
}
```  
**수강신청 처리**
```
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    // 큐에서 메시지를 소비하여 수강신청 처리
    @RabbitListener(queues = "course-registration-queue")
    public void processRegistration(RegistrationRequest request) {
        // 수강신청 처리 로직
        System.out.println("수강신청 처리 중: " + request.getCourseName() + " by " + request.getStudentName());
    }
}
```

-> 수많은 수강신청 요청을 큐에 비동기적으로 넣어 처리

**고려해야할 문제**
- 컨슈머 수가 적으면 큐가 밀려 지연 발생
- 실제 수강신청에서는 중복 신청 방지, 정원 초과 방지 필요
- 따라서 → Redis를 활용한 분산 락 필요


# 3. Redis 분산락: 동시성 문제 해결
- Redisson을 이용한 Redis 기반 분산 락을 통해 하나의 강좌에 대한 동시 신청을 제어
- 락을 걸고 있는 동안 다른 사용자의 접근은 대기 or 실패
- 정원 체크도 Redis로 관리하여 빠르고 정확하게 처리

**Redis & Redisson 설정**
```
// com.RabbitMQ.RabbitMQTest.config.RedisConfig.java
package com.RabbitMQ.RabbitMQTest.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    // Redisson 클라이언트 설정 (단일 Redis 서버)
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // Redis가 로컬 기본 포트라면
        config.useSingleServer().setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }
}
```  
-> Redisson 클라이언트가 직접 관리하기 때문에 application.properties에 설정 추가X

**Consumer에 분산락 로직 추가**
```
@RabbitListener(queues = "course-registration-queue")
public void consume(RegistrationRequest request) {
    String courseId = request.getCourseId();
    String lockKey = "lock:course:" + courseId;
    RLock lock = redissonClient.getLock(lockKey);

    try {
        // 락을 최대 5초 동안 시도, 락 획득 후 10초 뒤 자동 해제
        if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {

            // Redis 키로 수강신청된 인원 수 관리
            String countKey = "course:" + courseId + ":count";

            int current = Optional.ofNullable(
                redisTemplate.opsForValue().get(countKey)
            ).map(Integer::parseInt).orElse(0);

            int maxCapacity = 30;

            if (current < maxCapacity) {
                // 수강신청 인원 수 증가
                redisTemplate.opsForValue().increment(countKey);
                System.out.println("✅ 수강신청 성공 - " + request.getStudentName());
            } else {
                System.out.println("❌ 수강신청 실패 - 정원 초과");
            }
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // 스레드 복원
    } finally {
        // 락을 가진 스레드만 락 해제
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```  
-> 락 획득에 최대 5초, 획득 후 10초 뒤 자동 해제

---

**자세한 내용은 아래 링크에서 확인** 👇  
[RabbitMQ와 Redis로 수강 신청 시스템의 동시성 제어 실습 정리](https://hammerhead-horse-801.notion.site/RabbitMQ-Redis-1da574e302598092a491c8a9c8988ed2?pvs=4)
