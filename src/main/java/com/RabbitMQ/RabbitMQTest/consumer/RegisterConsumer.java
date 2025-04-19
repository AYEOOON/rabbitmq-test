package com.RabbitMQ.RabbitMQTest.consumer;

import com.RabbitMQ.RabbitMQTest.dto.RegisterRequest;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Getter
@Service
@RequiredArgsConstructor
public class RegisterConsumer{

    private final CountDownLatch latch = new CountDownLatch(1);
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    @RabbitListener(queues = "register.queue")
    public void consume(RegisterRequest request) {
        String courseId  = request.getCourseId();
        String studentId = request.getStudentId();
        String thread = Thread.currentThread().getName();

        String lockKey = "lock:course:" + courseId;
        RLock lock = redissonClient.getLock(lockKey);

        System.out.println(thread + " ▶ 락 시도: " + lockKey + " (" + studentId + ")");
        boolean gotLock = false;
        try {
            // 최대 5초 기다려서 락 획득 시도, 획득 후 10초 자동 해제
            gotLock = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!gotLock) {
                System.out.println("⛔ 락 획득 실패: 다른 요청 처리 중 (" + courseId + ")");
                return;
            }

            // Redis에 현재 신청 인원 조회
            System.out.println(thread + " ◀ 락 획득: " + lockKey + " (" + studentId + ")");
            String countKey = "course:" + courseId + ":count";
            int current = Optional.ofNullable(redisTemplate.opsForValue().get(countKey))
                    .map(Integer::parseInt)
                    .orElse(0);
            int maxCapacity = 30; // 예시: 최대 30명

            if (current < maxCapacity) {
                // increment() 이후 current + 1 출력하는 부분에서 race condition 생길 수 있음 -> → 정확한 count를 출력하고 싶다면 increment()의 반환값을 사용
                Long updated = redisTemplate.opsForValue().increment(countKey);
                System.out.println("✅ 수강신청 성공: " + studentId + " -> " + courseId
                        + " (현재 인원: " + updated + ")");
            } else {
                System.out.println("❌ 정원 초과: " + courseId + " (현재 인원: " + current + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (gotLock && lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println(thread + " ▶ 락 해제: " + lockKey + " (" + studentId + ")");
            }
            latch.countDown();
        }
    }

//    public class NoLockRegisterConsumer {
//
//        // 테스트에서 대기하려면 latch를 그대로 사용해도 되고, 없애도 무방합니다
//        private final CountDownLatch latch = new CountDownLatch(1);
//        private final RedisTemplate<String, String> redisTemplate;
//
//        @RabbitListener(queues = "register.queue")
//        public void consume(RegisterRequest request) {
//            String courseId = request.getCourseId();
//            String studentId = request.getStudentId();
//
//            // **락 없이** 바로 increment
//            String countKey = "course:" + courseId + ":count";
//            Long updated = redisTemplate.opsForValue().increment(countKey);
//            System.out.println("✅ (NoLock) 수강신청: "
//                    + studentId + " -> " + courseId
//                    + " (현재 인원: " + updated + ")");
//
//            latch.countDown();
//        }
//    }
}
