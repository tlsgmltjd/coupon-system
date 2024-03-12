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

}
