package com.example.consumer.consumer;

import com.example.consumer.domain.Coupon;
import com.example.consumer.domain.FailedEvent;
import com.example.consumer.repository.CouponJpaRepository;
import com.example.consumer.repository.FailedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CouponCreateConsumer {

    private final CouponJpaRepository couponJpaRepository;
    private final FailedEventRepository failedEventRepository;
    private final Logger logger = LoggerFactory.getLogger(CouponCreateConsumer.class);

    public CouponCreateConsumer(CouponJpaRepository couponJpaRepository, FailedEventRepository failedEventRepository) {
        this.couponJpaRepository = couponJpaRepository;
        this.failedEventRepository = failedEventRepository;
    }

    @KafkaListener(topics = "coupon_create", groupId = "group_1")
    public void listener(Long userId) {
        try {
            couponJpaRepository.save(new Coupon(userId));
        } catch (Exception e) {
            logger.error("failed to create coupon::" + userId);
            failedEventRepository.save(new FailedEvent(userId));
        }

        // + 쿠폰 저장에 userId를 FailedEvent를 디비에 저장한다.

        // 완성된 로직
        // API(Producer) -> Topic <-
        // Consumer(실패시) -> 쿠폰 발급
        // Consumer(실패시) -> FailedEvent를 생성한다 -> 후에 저장된 FailedEvent를 모두 쿠폰 디비에 저장해준다면 정상적으로 모든 쿠폰이 발급될 것이다.
    }

    // 테스트 케이스를 실행시키면 쿠폰이 적게 생성된걸 확인할 수 있다.
    // 왜냐하면 테스트 케이스에서는 발급 후 바로 갯수를 검증한다 (데이터 전송이 완료된 시점에 검증한다)
    // 하지만 카프카는 데이터 처리를 실시간으로 하지 않기 때문이다

    /*
    *
    * 10:00 테스트 케이스 시작 | | 데이터 수신중
    * 10:01 | 데이터 전송완료 | 데이터 처리중
    * 10:02 테스트 케이스 종료 | | 데이터 처리중
    * 10:03 | | 데이터 처리중
    * 10:04 | | 데이터 처리완료
    *
    * */

    // Thread.sleep() 을 걸어서 잠시 기다린 후 검증을 시도하면 성공한다.

    // 카프카를 사용하면 api 모듈에서 쿠폰을 생성하는 로직에 비해 처리량을 조절해주며 데이터베이스의 부하를 줄일 수 있다.

}
