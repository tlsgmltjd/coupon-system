package com.example.consumer.consumer;

import com.example.consumer.domain.Coupon;
import com.example.consumer.repository.CouponJpaRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CouponCreateConsumer {

    private final CouponJpaRepository couponJpaRepository;

    public CouponCreateConsumer(CouponJpaRepository couponJpaRepository) {
        this.couponJpaRepository = couponJpaRepository;
    }

    @KafkaListener(topics = "coupon_create", groupId = "group_1")
    public void listener(Long userId) {
        couponJpaRepository.save(new Coupon(userId));
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
